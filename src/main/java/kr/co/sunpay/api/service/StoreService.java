package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	public Store create(Store store) {

		log.info("-- StoreService.create called...");
		// group id check
		if (store.getGroup() == null) {
			throw new IllegalArgumentException("The Required Parameter('group':{'uid': ''}) is missing.");
		}

		return storeRepo.save(store);
	}

	/**
	 * groupUid 자신 그룹과 하위 모든 그룹이 가진 상점 리스트 반환
	 * 
	 * @param groupUid
	 * @return
	 */
	public List<Store> getStores(int groupUid) {

		List<Store> stores = new ArrayList<Store>();
		stores = storeRepo.findByGroup(groupUid);

		return stores;
	}

	public boolean isInstantOn(String storeId) {

		if (storeIdRepo.findByIdAndActivated(storeId, true).isPresent())
			return true;

		return false;
	}
}
