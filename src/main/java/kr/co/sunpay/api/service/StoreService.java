package kr.co.sunpay.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.repository.StoreRepository;
import lombok.extern.java.Log;

@Log
@Service
public class StoreService {

	@Autowired
	StoreRepository storeRepo;
	
	public Store create(Store store) {
		
		log.info("-- StoreService.create called...");
		return storeRepo.save(store);
	}
}
