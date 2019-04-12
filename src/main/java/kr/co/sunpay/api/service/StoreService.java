package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.FcmToken;
import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.model.MemberResponse;
import kr.co.sunpay.api.model.StoreRequest;
import kr.co.sunpay.api.repository.StoreIdRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import kr.co.sunpay.api.util.Sunpay;

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
	PushService pushService;

	public static final String SERVICE_TYPE_INSTANT = "INSTANT";
	public static final String SERVICE_TYPE_D2 = "D2";

	public static final String BIZ_TYPE_NONE = "NONE";
	public static final String BIZ_TYPE_CORPORATION = "CORPORATION";
	public static final String BIZ_TYPE_INDIVIDUAL = "INDIVIDUAL";
	
	public static final String STATE_NEW = "NEW";
	public static final String STATE_UPLOADED = "UPLOADED";
	public static final String STATE_DOCUMENT_PASS = "DOCUMENT_PASS";
	public static final String STATE_PASS = "PASS";
	public static final String STATE_REJECTED = "REJECTED";
	public static final String STATE_REVIEW = "REVIEW";

	public MemberResponse findOwner(Store store) {
		
		Iterator<Member> members = store.getMembers().iterator();
		
		while (members.hasNext()) {
			Member owner = members.next();
			
			if (hasRole(owner, MemberService.ROLE_OWNER)) return new MemberResponse(owner);
		}
		
		return null;
	}
	
	/**
	 * 상점 데이터 검사기
	 * 
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

			if (!hasRole(owner, ROLE_STORE)) {
				throw new IllegalArgumentException("Store member should have STORE role.");
			}
		}

		// 최소예치금 검사
		if (!(store.getMinDeposit() > 0)) {
			throw new IllegalArgumentException("Minimum depoist required.");
		}

		return true;
	}

	/**
	 * 상점 등록 시 담당자 정보 검사
	 * 
	 * @param owner
	 * @return
	 */
	public boolean validatorOwner(Member owner) {

		// 권한 검사
		if (Sunpay.isEmpty(owner.getRoles()) || !hasRole(owner, ROLE_STORE) || !hasRole(owner, ROLE_OWNER)
				|| !hasRole(owner, ROLE_MANAGER)) {
			throw new IllegalArgumentException("담당자 필수 권한이 누락되었습니다.");
		}

		// 중복검사 - 아이디, 이메일, TODO:모바일
		if (hasMember(owner.getId())) {
			throw new DuplicateKeyException("담당자 아이디가 이미 사용중입니다.");
		}

		if (countMail(owner.getEmail()) > 0) {
			throw new DuplicateKeyException("담당자 이메일이 이미 사용중입니다.");
		}

		return true;
	}

	/**
	 * 상점 등록 시 사업자 정보 검사
	 * 
	 * @param store
	 * @return
	 */
	public boolean validatorBiz(Store store) {

		// 비사업자 데이터 체크
		if (store.getBizTypeCode().equals(BIZ_TYPE_NONE)) {

			// 개인사업자 데이터 체크
		} else if (store.getBizTypeCode().equals(BIZ_TYPE_INDIVIDUAL)) {

			if (Sunpay.isEmpty(store.getBizNo())) {
				throw new IllegalArgumentException("사업자등록번호가 누락되었습니다.");
			}

			// 법인사업자 데이터 체크
		} else if (store.getBizTypeCode().equals(BIZ_TYPE_CORPORATION)) {

			if (Sunpay.isEmpty(store.getBizNo())) {
				throw new IllegalArgumentException("사업자등록번호가 누락되었습니다.");
			}
		}

		return true;
	}

	/**
	 * memberUid로 접근 가능한 상점인 경우 상점 데이터 업데이트
	 * 
	 * @param storeUid
	 * @param store
	 * @param memberUid
	 * @return
	 */
	public Store update(int storeUid, Store store, int memberUid) {

		Store updatedStore = getStore(storeUid);
		if (updatedStore == null)
			throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");

		// 상점 수정 정보 검사
		updateValidator(store);

		// 수정 가능한 항목만 수정함
		// 사업자정보
		updatedStore.setBizOwner(store.getBizOwner());
		updatedStore.setBizOwnerRegiNo(store.getBizOwnerRegiNo());
		updatedStore.setBizName(store.getBizName());
		updatedStore.setBizAddressBasic(store.getBizAddressBasic());
		updatedStore.setBizAddressDetail(store.getBizAddressDetail());
		updatedStore.setBizContact(store.getBizContact());

		// 가입비
		updatedStore.setMembershipFee(store.getMembershipFee());

		storeRepo.save(updatedStore);

		return updatedStore;

	}

	/**
	 * 상점 정보 수정 시 데이터 점검, 문제 시 throws exception
	 * 
	 * @param store
	 */
	public void updateValidator(Store store) {

		if (isEmpty(store.getBizOwner())) {
			throw new IllegalArgumentException("필수정보 미입력(사업자정보 - 성명)");
		}

		if (isEmpty(store.getBizOwnerRegiNo())) {
			throw new IllegalArgumentException("필수정보 미입력(사업자정보 - 주민번호)");
		}

		if (isEmpty(store.getBizName())) {
			throw new IllegalArgumentException("필수정보 미입력(사업자정보 - 사업장명)");
		}
	}

	/**
	 * memberUid의 멤버가 groupUid의 그룹에 대한 관리권한이 있는지 확인
	 * 
	 * @param memberUid
	 * @param groupUid
	 * @return
	 */
	public boolean isAdminable(int memberUid, int groupUid) {

		try {

			Iterator<Group> iGroups = groupService.getGroups(memberUid).iterator();
			while (iGroups.hasNext()) {
				if (iGroups.next().getUid() == groupUid) {
					return true;
				}
			}

		} catch (Exception e) {
			// 오류 발생 시 권한 없음으로 반환
			return false;
		}

		return false;
	}

	/**
	 * 상점 소속의 Manager 권한이 있는 멤버인지 확인
	 * 
	 * @param memberUid
	 * @param store
	 * @return
	 */
	public boolean isStoreManager(int memberUid, Store store) {

		Member manager = getMember(memberUid);

		if (hasRole(manager, ROLE_MANAGER)) {
			if (manager.getStore().getUid() == store.getUid()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * StoreRequest 받아서 Store Entity 생성
	 * 
	 * @param storeReq
	 * @return
	 */
	@Transactional
	public Store regist(StoreRequest storeReq) {

		Store store = storeReq.toEntity();
		
		// 상점 생싱 시 상태값 NEW로 초기화
		store.setStateCode(STATE_NEW);

		// 사업자 구분에 따른 데이터 체크
		validatorBiz(store);

		// 상위업체 정보 설정
		Group group = groupService.getGroup(storeReq.getGroupUid());

		if (Sunpay.isEmpty(group)) {
			throw new IllegalArgumentException("상위업체 정보를 찾을 수 없습니다.");
		}

		store.setGroup(groupService.getGroup(storeReq.getGroupUid()));

		// 담당자 정보 설정
		Member owner = storeReq.getMemberReq().toEntity(store);

		// 담당자 정보 체크
		validatorOwner(owner);

		// 담당자 비밀번호 암호화
		owner.setPassword(pwEncoder.encode(owner.getPassword()));

		List<Member> members = new ArrayList<Member>();
		members.add(owner);

		store.setMembers(members);

		// 상위업체별 수수료 정보 설정 - 본사 직접 가입인 경우 제외
		if (store.getGroup().getUid() != groupService.findHead().getUid()) {
			registFee(store);
		}

		// 예치금 번호 없으면 랜덤으로 생성, 있으면 중복검사
		if (Sunpay.isEmpty(store.getDepositNo())) {
			store.setDepositNo(createDepositNo());
		} else if (storeRepo.findByDepositNo(store.getDepositNo()).isPresent()) {
			throw new DuplicateKeyException("예치금 입금번호가 이미 사용중입니다.");
		}

		return storeRepo.save(store);
	}

	/**
	 * 멤버가 상점 등록 시, 멤버의 권한 확인 후 상점 등록
	 * 
	 * @param storeReq
	 * @param memberUid
	 * @return
	 */
	public Store regist(StoreRequest storeReq, int memberUid) {

		// memberUid 의 멤버가 생성하려는 상점의 상위업체에 대한 권한이 있는지 확인
		if (isAdminable(memberUid, storeReq.getGroupUid())) {
			return regist(storeReq);
		} else {
			throw new IllegalArgumentException("memberUid의 권한으로 생성할 수 없는 그룹 소속입니다.");
		}
	}

	/**
	 * 상점 등록 시 상위업체별 수수료 설정
	 * 
	 * @param store
	 */
	public void registFee(Store store) {

		Group group = store.getGroup();

		// PG 수수료는 본사 설정값에서 가져옴
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
	}

	/**
	 * memberUid 하위 소속으로 상점 생성
	 * 
	 * @param store
	 * @param memberUid
	 * @return
	 */
	public Store create(Store store, int memberUid) {

		if (isAdminable(memberUid, store.getGroupUid())) {
			return create(store);
		} else {
			throw new IllegalArgumentException("memberUid의 권한으로 생성할 수 없는 그룹 소속입니다.");
		}
	}

	/**
	 * 상점 생성
	 * 
	 * @param store
	 * @return
	 */
	public Store create(Store store) {

		log.info("-- StoreService.create called...");

		// 데이터 검사
		validator(store);

		// 상점 데이터 셋팅
		store.setDeposit(0);

		// 상점ID는 상점ID 등록 API를 이용해야 함
		store.setStoreIds(null);

		// OWNER 멤버 등록
		List<Member> members = new ArrayList<Member>();

		// 소유주 멤버 데이터 셋팅
		Member owner = store.getMembers().get(0);

		// 아이디 중복검사
		if (hasMember(owner.getId())) {
			throw new DuplicateKeyException("아이디 중복");
		}

		// 비밀번호 암호화
		owner.setPassword(pwEncoder.encode(owner.getPassword()));

		// OWNER 권한 확인(상점 생성 시 멤버는 STORE, MANAGER, OWNER 권한을 default로 갖는다)
		if (!hasRole(owner, ROLE_MANAGER) || !hasRole(owner, ROLE_OWNER)) {
			throw new IllegalArgumentException("Store owner member should have OWNER and MANAGER roles.");
		}
		members.add(owner);
		store.setMembers(members);

		// <- 상점 생성 시 수수료 데이터 셋팅 시작
		Group group = groupService.getGroup(store.getGroup().getUid());
		store.setGroup(group);

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

	/**
	 * 상점 리턴, 없는 경우 null 리턴
	 * 
	 * @param storeUid
	 * @return
	 */
	public Store getStore(int storeUid) {

		Store store = storeRepo.findByUid(storeUid).orElse(null);

		if (store == null) {
			return null;
		}

		store.setGroupName(store.getGroup().getBizName());
		store.setGroupUid(store.getGroup().getUid());

		return store;
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
	 * 
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
	 * 
	 * @param member
	 * @return
	 */
	public List<Store> getStoresByMember(Member member) {

		List<Store> stores = new ArrayList<Store>();

		// 최고관리자 또는 본사 멤버인 경우 모든 상점리스트 반환
		if (hasRole(member, ROLE_TOP) || hasRole(member, ROLE_HEAD)) {
			stores = getStoresByGroup(member.getGroup());
		}

		// 상점 멤버인 경우 해당 상점만 반환
		if (hasRole(member, ROLE_STORE)) {
			stores.add(member.getStore());
			return stores;
		}

		// 대리점 멤버인 경우 해당 대리점의 상점리스트 반환
		if (hasRole(member, ROLE_AGENCY)) {
			stores = member.getGroup().getStores();
		}

		// 지사 멤버인 경우 해당 지사와 하위 대리점 소속의 상점리스트 반환
		if (hasRole(member, ROLE_BRANCH)) {
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
	 * 
	 * @param storeId
	 * @return
	 */
	public boolean isInstantOn(String storeId) {

		if (storeId != null && storeIdRepo.findByIdAndActivated(storeId, true).isPresent())
			return true;

		return false;
	}

	/**
	 * 현재 활성상태의 상점ID 리턴
	 * 
	 * @param store
	 * @return
	 */
	public String getActivatedId(Store store) {

		StoreId storeId = storeIdRepo.findByStoreAndActivated(store, true).orElse(null);
		if (!Sunpay.isEmpty(storeId)) {
			return storeId.getId();
		}

		return null;
	}

	/**
	 * 상점 ID로 상점 리턴
	 * 
	 * @param storeId
	 * @return
	 */
	public Store getStoreByStoreId(String storeId) {

		StoreId id = storeIdRepo.findById(storeId).orElse(null);

		if (id != null) {
			Store store = storeRepo.findByStoreIds(id).orElse(null);
			return store;
		}

		return null;
	}

	/**
	 * 상점ID 리스트 리턴
	 * 
	 * @param storeUid
	 * @return
	 */
	public List<StoreId> getStoreIds(int storeUid) {
		Store store = getStore(storeUid);
		if (store == null)
			return null;

		return store.getStoreIds();
	}

	public Store createStoreIds(int storeUid, List<StoreId> ids) {

		List<StoreId> nIds = new ArrayList<StoreId>();

		// 중복 ID 확인
		for (StoreId id : ids) {
			if (getStoreId(id.getId()) != null) {
				throw new DuplicateKeyException("상점ID는 중복될 수 없습니다.");
			}

			// 상점ID 생성 시 ID, ServiceTypeCode 값만 받아서 생성함
			StoreId nId = new StoreId();
			nId.setId(id.getId());
			nId.setServiceTypeCode(id.getServiceTypeCode());
			nIds.add(nId);
		}

		// 상점 정보 가져오기
		Store store = getStore(storeUid);
		if (store == null)
			throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");

		// 새 상점ID 리스트 추가
		store.getStoreIds().addAll(nIds);

		// ServiceTypeCode 중복 체크
		if (isStoreIdTypeDuplicated(store.getStoreIds()))
			throw new DuplicateKeyException("정산타입이 중복됩니다.");

		// 상점 정보 저장
		storeRepo.save(store);
		return store;
	}

	/**
	 * 상점ID 삭제
	 * 
	 * @param storeUid
	 * @param storeId
	 */
	public void deleteStoreId(int storeUid, String storeId) {

		Store store = getStore(storeUid);

		if (store == null)
			throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");

		for (Iterator<StoreId> iter = store.getStoreIds().iterator(); iter.hasNext();) {
			StoreId id = iter.next();

			if (id.getId().equals(storeId)) {
				iter.remove();
			}
		}

		storeRepo.save(store);
	}

	/**
	 * 상점ID 활성화
	 * 
	 * @param storeUid
	 * @param storeId
	 */
	public void activateStoreId(int storeUid, String storeId) {

		Store store = getStore(storeUid);

		if (store == null)
			throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");

		if (!storeIdRepo.findByIdAndStore(storeId, store).isPresent())
			throw new IllegalArgumentException("상점ID를 찾을 수 없습니다.");

		Iterator<StoreId> iter = store.getStoreIds().iterator();
		while (iter.hasNext()) {
			StoreId id = iter.next();
			if (storeId.equals(id.getId())) {
				id.setActivated(true);
			} else {
				id.setActivated(false);
			}
		}

		storeRepo.save(store);
	}

	/**
	 * 순간정산 활성화, 성공 시 true 리턴
	 * 
	 * @param storeUid
	 */
	public boolean instantOn(int storeUid, boolean sendPush) {

		Store store = getStore(storeUid);

		if (store == null)
			throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");

		// 예치금이 최소예치금보다 작은 경우 활성화 안됨, 리턴
		if (store.getDeposit() < store.getMinDeposit()) {
			instantOff(storeUid, sendPush);
			throw new IllegalArgumentException("예치금이 부족합니다.");
		}

		// 순간정산인 경우 리턴함
		StoreId nowId = store.getActivatedId();

		if (!isEmpty(nowId) && nowId.getServiceTypeCode().equals(SERVICE_TYPE_INSTANT)) {
			return true;
		}

		boolean isInstantOn = false;
		Iterator<StoreId> iter = store.getStoreIds().iterator();
		while (iter.hasNext()) {
			StoreId id = iter.next();
			if (id.getServiceTypeCode().equals(SERVICE_TYPE_INSTANT)) {
				id.setActivated(true);
				isInstantOn = true;
			} else {
				id.setActivated(false);
			}
		}

		// 순간정산ID 찾았을 경우에만 저장
		if (isInstantOn) {
			storeRepo.save(store);

			if (sendPush)
				pushInstantOn(store);
		}

		return isInstantOn;
	}

	/**
	 * 순간정산 비활성화, 일반정산으로 변경 PUSH 알람 발송됨
	 * 
	 * @param store
	 * @param sendPush
	 * @return
	 */
	public boolean instantOff(Store store, boolean sendPush) {

		if (store == null)
			throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");

		// 순간정산이 아닌경우 리턴함
		StoreId nowId = store.getActivatedId();

		if (!isEmpty(nowId) && !nowId.getServiceTypeCode().equals(SERVICE_TYPE_INSTANT)) {
			return true;
		}

		boolean isInstantOff = false;
		Iterator<StoreId> iter = store.getStoreIds().iterator();
		while (iter.hasNext()) {
			StoreId id = iter.next();
			if (id.getServiceTypeCode().equals(SERVICE_TYPE_INSTANT)) {
				id.setActivated(false);
			} else {
				// TODO: Default로 설정된 일반정산ID를 사용하도록 변경해야 함
				// 현재: 처음으로 가져오는 일반정산ID 활성화함
				if (!isInstantOff) {
					id.setActivated(true);
					isInstantOff = true;
				} else {
					id.setActivated(false);
				}
			}
		}

		// 순간정산ID 찾았을 경우에만 저장
		if (isInstantOff) {
			storeRepo.save(store);

			// 순간 > 일반 전환 PUSH 발송
			if (sendPush)
				pushInstantOff(store);

		}

		return isInstantOff;
	}

	/**
	 * 순간정산 비활성화, 일반정산으로 변경
	 * 
	 * @param storeUid
	 * @param sendPush
	 * @return
	 */
	public boolean instantOff(int storeUid, boolean sendPush) {

		Store store = getStore(storeUid);
		return instantOff(store, sendPush);

	}

	/**
	 * 순간정산 비활성화 PUSH 알림
	 * 
	 * @param store
	 */
	public void pushInstantOn(Store store) {

		List<FcmToken> tokens = pushService.getTokensByStore(store);

		// 순간정산 비활성화 메세지 작성
		Map<String, String> msg = new HashMap<String, String>();
		String msgText = "순간정산이 활성화되었습니다.";

		msg.put("cate", "system");
		msg.put("isDisplay", "Y");
		msg.put("title", "순간정산 ON");
		msg.put("message", msgText);

		tokens.forEach(token -> {
			msg.put("user", token.getId());
			pushService.push(token.getFcmToken(), msg);
		});

	}

	/**
	 * 순간정산 비활성화 PUSH 알림
	 * 
	 * @param store
	 */
	public void pushInstantOff(Store store) {

		List<FcmToken> tokens = pushService.getTokensByStore(store);

		// 순간정산 비활성화 메세지 작성
		Map<String, String> msg = new HashMap<String, String>();
		String msgText = "순간정산이 비활성화되었습니다.";

		msg.put("cate", "system");
		msg.put("isDisplay", "Y");
		msg.put("title", "순간정산 OFF");
		msg.put("message", msgText);

		tokens.forEach(token -> {
			msg.put("user", token.getId());
			pushService.push(token.getFcmToken(), msg);
		});

	}

	public StoreId getStoreId(String id) {

		return storeIdRepo.findById(id).orElse(null);
	}

	/**
	 * 서비스타입코드값으로 상점의 상점ID 반환
	 * 
	 * @param store
	 * @param serviceTypeCode
	 * @return
	 */
	public String getStoreIdByServiceType(Store store, String serviceTypeCode) {

		for (StoreId storeId : store.getStoreIds()) {
			if (storeId.getServiceTypeCode().equals(serviceTypeCode)) {
				return storeId.getId();
			}
		}

		return null;
	}

	/**
	 * 상점ID 리스트중 정산타입 중복이 있으면 true 리턴
	 * 
	 * @param store
	 * @return
	 */
	public boolean isStoreIdTypeDuplicated(List<StoreId> ids) {

		String typeList = "";

		for (StoreId id : ids) {
			if (typeList.indexOf(id.getServiceTypeCode()) < 0) {
				typeList += id.getServiceTypeCode();
			} else {
				return true;
			}
		}

		return false;
	}

	/**
	 * 기존 상점ID를 uId로 업데이트(ServiceTypeCode가 맞을때만)
	 * 
	 * @param storeUid
	 * @param id
	 * @return
	 */
	public Store updateStoreId(Store store, StoreId uId) {

		// 상점정보 확인
		if (store == null)
			throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");

		// 상점ID 중복여부 확인
		StoreId xId = getStoreId(uId.getId());

		if (xId != null) {

			if (xId.getStore().getUid() == store.getUid()) {
				throw new IllegalArgumentException("기존 ID와 같습니다.");
			}

			throw new DuplicateKeyException("상점ID는 중복될 수 없습니다.");
		}

		boolean isUpdated = false;

		for (Iterator<StoreId> iter = store.getStoreIds().iterator(); iter.hasNext();) {
			StoreId oId = iter.next();

			if (oId.getServiceTypeCode().equals(uId.getServiceTypeCode())) {
				oId.setId(uId.getId());
				isUpdated = true;
			}
		}

		if (isUpdated) {
			storeRepo.save(store);
		}

		return store;
	}
}
