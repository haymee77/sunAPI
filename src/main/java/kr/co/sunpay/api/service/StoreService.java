package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
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
public class StoreService {

	@Autowired
	StoreRepository storeRepo;

	@Autowired
	StoreIdRepository storeIdRepo;
	
	@Autowired
	GroupService groupService;
	
	@Autowired
	MemberService memberService;

	public Store create(Store store) {

		log.info("-- StoreService.create called...");
		// group id check
		if (store.getGroup() == null) {
			throw new IllegalArgumentException("The Required Parameter('group':{'uid': ''}) is missing.");
		}

		return storeRepo.save(store);
	}
	
	public Store getStore(int storeUid) {
		
		Optional<Store> oStore = storeRepo.findByUid(storeUid);
		
		if (!oStore.isPresent()) {
			throw new EntityNotFoundException("There is no Store available");
		}
		
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
		
		group.getStores().forEach(s -> {
			stores.add(s);
		});
		
		// 하위 그룹의 상점 가져오기
		List<Group> children = groupService.getChildren(group);
		
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
			return storeRepo.findAll();
		}
		
		// 상점 멤버인 경우 해당 상점만 반환 
		if (memberService.hasRole(member, MemberService.ROLE_STORE)) {
			stores.add(getStore(member.getStoreUid()));
			return stores;
		}
		
		// 대리점 멤버인 경우 해당 대리점의 상점리스트 반환
		if (memberService.hasRole(member, MemberService.ROLE_AGENCY)) {
			return groupService.getGroup(member.getGroupUid()).getStores();
		}
		
		if (memberService.hasRole(member, MemberService.ROLE_BRANCH)) {
			return getStoresByGroup(member.getGroup());
		}
		
		return stores;
	}
	
	public boolean isInstantOn(String storeId) {

		if (storeIdRepo.findByIdAndActivated(storeId, true).isPresent())
			return true;

		return false;
	}
}
