package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.repository.StoreIdRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import lombok.extern.java.Log;

@Log
@Service
public class StoreService extends MemberService {

	@Autowired
	StoreRepository storeRepo;

	@Autowired
	StoreIdRepository storeIdRepo;
	
	@Autowired
	PasswordEncoder pwEncoder;
	
	@Autowired
	GroupService groupService;
	
	@Autowired
	MemberService memberService;
	
	/**
	 * 상점 데이터 검사기
	 * @param store
	 * @return
	 */
	public boolean validator(Store store) {
		
		// 상위 그룹 검사
		if (store.getGroup() == null) {
			throw new IllegalArgumentException("The Required Parameter('group':{'uid': ''}) is missing.");
		}

		// 소속 멤버 검사
		if (store.getMembers().size() != 1) {
			throw new IllegalArgumentException("Owner member should be one");
		} else {
			Member owner = store.getMembers().get(0);
			
			if (owner.getRoles() == null || owner.getRoles().size() < 1) {
				throw new IllegalArgumentException("Store member has no role.");
			}
			
			if (!memberService.hasRole(owner, MemberService.ROLE_STORE)) {
				throw new IllegalArgumentException("Store member should have STORE role.");
			}
		}

		// 상점 ID 검사
		if (store.getStoreIds() == null || store.getStoreIds().size() < 1) {
			throw new IllegalArgumentException("At least one store ID is required.");
		} else if (store.getStoreIds().size() > 2) {
			throw new IllegalArgumentException("Can not create store ID more than 2.");
		} else {
			int activeCnt = 0;
			for (StoreId id : store.getStoreIds()) {
				if (id.getActivated()) activeCnt++;
				if (activeCnt > 1) {
					throw new IllegalArgumentException("Activated store ID should be one.");
				}
			}
		}
		
		return true;
	}
	
	/**
	 * memberUid로 접근 가능한 상점인 경우 상점 데이터 업데이트
	 * @param storeUid
	 * @param store
	 * @param memberUid
	 * @return
	 */
	public Store update(int storeUid, Store store, int memberUid) {
		
		if (!isAdminable(memberUid, store) && !isStoreManager(memberUid, store)) {
			throw new IllegalArgumentException("memberUid의 권한으로 수정할 수 없는 그룹 소속입니다.");
		}

		Store updatedStore = getStore(storeUid);

		// 수정 가능한 항목만 수정함
		updatedStore.setBizName(store.getBizName());

		storeRepo.save(updatedStore);

		return updatedStore;
		
	}
	
	/**
	 * 관리 가능한 멤버인지 확인(상점 소속 그룹을 포함한 상위그룹의 멤버인지 확인)
	 * ** 상점의 매니저 권한은 {@link #isStoreManager(int, Store)} 로 확인
	 * @param memberUid
	 * @param store
	 * @return
	 */
	public boolean isAdminable(int memberUid, Store store) {
		// 상위 그룹 검사
		if (store.getGroup() == null) {
			throw new IllegalArgumentException("The Required Parameter('group':{'uid': ''}) is missing.");
		}
		
		// memberUid 권한으로 접근 가능한 그룹 리스트에 소속되는 상점인지 확인
		try {
			List<Group> managerGroups = groupService.getGroups(memberUid);
			
			for (Group g : managerGroups) {
				if (store.getGroup().getUid() == g.getUid()) {
					return true;
				}
			}
		} catch (Exception ex) {
			return false;
		}
		
		return false;
	}
	
	/**
	 * 상점 소속의 Manager 권한이 있는 멤버인지 확인
	 * @param memberUid
	 * @param store
	 * @return
	 */
	public boolean isStoreManager(int memberUid, Store store) {
		
		Member manager = memberService.getMember(memberUid);
		
		if (memberService.hasRole(manager, MemberService.ROLE_MANAGER)) {
			if (manager.getStoreUid() == store.getUid()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * memberUid 하위 소속으로 상점 생성
	 * @param store
	 * @param memberUid
	 * @return
	 */
	public Store create(Store store, int memberUid) {
		
		if (isAdminable(memberUid, store)) {
			return create(store);
		} else {
			throw new IllegalArgumentException("memberUid의 권한으로 생성할 수 없는 그룹 소속입니다.");
		}
	}

	/**
	 * 상점 생성
	 * @param store
	 * @return
	 */
	public Store create(Store store) {

		log.info("-- StoreService.create called...");
		
		// 데이터 검사
		validator(store);
		
		// 상점 데이터 셋팅
		store.setDeposit(0);
		
		// OWNER 멤버 등록
		List<Member> members = new ArrayList<Member>();
		
		// 소유주 멤버 데이터 셋팅
		Member owner = store.getMembers().get(0);
		
		// 아이디 중복검사
		if (memberService.hasMember(owner.getId())) {
			throw new DuplicateKeyException("아이디 중복");
		}
		
		// 비밀번호 암호화
		owner.setPassword(pwEncoder.encode(owner.getPassword()));
		
		// OWNER 권한 확인(상점 생성 시 멤버는 STORE, MANAGER, OWNER 권한을 default로 갖는다)
		if (!memberService.hasRole(owner, MemberService.ROLE_MANAGER)
				|| !memberService.hasRole(owner, MemberService.ROLE_OWNER)) {
			throw new IllegalArgumentException("Store owner member should have OWNER and MANAGER roles.");
		}
		members.add(store.getMembers().get(0));
		store.setMembers(members);
		
		// <- 상점 생성 시 수수료 데이터 셋팅 시작
		Group group = store.getGroup();
		
		// - PG 수수료는 환경설정에서 가져옴
		store.setFeePg(groupService.getConfig().getFeePg());
		store.setTransFeePg(groupService.getConfig().getTransFeePg());
		
		switch (group.getRoleCode()) {
		case GroupService.ROLE_HEAD:
			if (!(store.getFeeHead() > 0)) {
				throw new IllegalArgumentException("수수료 미입력");
			}
			
			if (!(store.getTransFeeHead() > 0)) {
				throw new IllegalArgumentException("순간정산수수료 미입력");
			}
			
			store.setFeeBranch(0.0);
			store.setFeeAgency(0.0);
			store.setTransFeeBranch(0);
			store.setTransFeeAgency(0);
			break;

		case GroupService.ROLE_BRANCH:
			if (!(store.getFeeBranch() > 0)) {
				throw new IllegalArgumentException("수수료 미입력");
			}
			if (!(store.getTransFeeBranch() > 0)) {
				throw new IllegalArgumentException("순간정산수수료 미입력");
			}
			
			store.setFeeHead(group.getFeeHead());
			store.setFeeAgency(0.0);
			store.setTransFeeHead(group.getTransFeeHead());
			store.setTransFeeAgency(0);
			break;
			
		case GroupService.ROLE_AGENCY:
			if (!(store.getFeeAgency() > 0)) {
				throw new IllegalArgumentException("수수료 미입력");
			}
			if (!(store.getTransFeeAgency() > 0)) {
				throw new IllegalArgumentException("순간정산수수료 미입력");
			}
			
			store.setFeeHead(group.getFeeHead());
			store.setFeeBranch(group.getFeeBranch());
			store.setTransFeeHead(group.getTransFeeHead());
			store.setTransFeeBranch(group.getTransFeeBranch());
			break;
		}
		// 상점 생성 시 수수료 데이터 셋팅 끝 ->
		
		// 상점 생성 후 예치금 번호 생성
		Store newStore = storeRepo.save(store);
		newStore.setDepositNo(createDepositNo());

		return storeRepo.save(newStore);
	}
	
	public String createDepositNo() {
		int randNo = ThreadLocalRandom.current().nextInt(100000, 999999 + 1);
		String depositNo = String.valueOf(randNo);
		
		if (storeRepo.findByDepositNo(depositNo).isPresent()) {
			return createDepositNo();
		} 
		
		return String.valueOf(randNo);
	}
	
	public Store getStore(int storeUid) {
		
		Optional<Store> oStore = storeRepo.findByUid(storeUid);
		
		if (!oStore.isPresent()) {
			throw new EntityNotFoundException("There is no Store available");
		}
		
		Store store = oStore.get();
		store.setGroupName(store.getGroup().getBizName());
		store.setGroupUid(store.getGroup().getUid());
		
		return oStore.get();
	}

	/**
	 * groupUid가 가진 상점리스트 반환
	 * 
	 * @param groupUid
	 * @return
	 */
	public List<Store> getStoresByGroup(int groupUid) {

		List<Store> stores = new ArrayList<Store>();
		
		Group group = groupService.getGroup(groupUid);
		
		group.getStores().forEach(s -> {
			stores.add(s);
		});

		return stores;
	}
	
	/**
	 * group 및 하위 group의 모든 상점 반환
	 * @param group
	 * @return
	 */
	public List<Store> getStoresByGroup(Group group) {
		List<Store> stores = new ArrayList<Store>();
		
		// 자신 소속 상점 가져오기
		group.getStores().forEach(s -> {
			stores.add(s);
		});
		
		// 하위 그룹의 상점 가져오기
		List<Group> children = groupService.getChildren(group, true);
		
		if (children != null) {
			children.forEach(g -> {
				g.getStores().forEach(s -> {
					stores.add(s);
				});
			});
		}
		
		return stores;
	}

	/**
	 * member 권한으로 접근 가능한 모든 상점리스트 반환
	 * @param member
	 * @return
	 */
	public List<Store> getStoresByMember(Member member) {
		
		List<Store> stores = new ArrayList<Store>();
		
		// 최고관리자 또는 본사 멤버인 경우 모든 상점리스트 반환
		if (memberService.hasRole(member, MemberService.ROLE_TOP)
				|| memberService.hasRole(member, MemberService.ROLE_HEAD)) {
			stores = getStoresByGroup(member.getGroup());
		}
		
		// 상점 멤버인 경우 해당 상점만 반환 
		if (memberService.hasRole(member, MemberService.ROLE_STORE)) {
			stores.add(getStore(member.getStoreUid()));
			return stores;
		}
		
		// 대리점 멤버인 경우 해당 대리점의 상점리스트 반환
		if (memberService.hasRole(member, MemberService.ROLE_AGENCY)) {
			stores = groupService.getGroup(member.getGroupUid()).getStores();
		}
		
		// 지사 멤버인 경우 해당 지사와 하위 대리점 소속의 상점리스트 반환
		if (memberService.hasRole(member, MemberService.ROLE_BRANCH)) {
			stores = getStoresByGroup(member.getGroup());
		}
		
		for (Store store : stores) {
			store.setGroupName(store.getGroup().getBizName());
			store.setGroupUid(store.getGroup().getUid());
		}
		
		return stores;
	}
	
	/**
	 * 순간정산 켜져있는지 확인
	 * @param storeId
	 * @return
	 */
	public boolean isInstantOn(String storeId) {

		if (storeIdRepo.findByIdAndActivated(storeId, true).isPresent())
			return true;

		return false;
	}
}
