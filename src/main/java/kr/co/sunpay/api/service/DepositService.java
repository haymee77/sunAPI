package kr.co.sunpay.api.service;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.repository.StoreRepository;

@Service
public class DepositService {

	@Autowired
	StoreRepository storeRepo;
	
	/**
	 * depositNo로 상점 검색해서 예치금 증액 및 히스토리 기록
	 * @param depositNo
	 * @param depositAmt
	 */
	public void deposit(String depositNo, int depositAmt) {
		
		Optional<Store> oStore = storeRepo.findByDepositNo(depositNo);
		if (!oStore.isPresent()) throw new EntityNotFoundException("상점을 찾을 수 없습니다.");
		
		Store store = oStore.get();
		store.setDeposit(store.getDeposit() + depositAmt);
		storeRepo.save(store);
	}
	
	public boolean isValidNo(String depositNo) {
		
		Optional<Store> oStore = storeRepo.findByDepositNo(depositNo);
		if (!oStore.isPresent()) return false;
		
		return true;
	}
	
	
}
