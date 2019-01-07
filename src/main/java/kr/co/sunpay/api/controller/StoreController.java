package kr.co.sunpay.api.controller;

import java.net.URI;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.repository.StoreRepository;
import kr.co.sunpay.api.service.StoreService;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/store")
public class StoreController {

	@Autowired
	StoreRepository storeRepo;
	
	@Autowired
	StoreService storeService;
	
	@GetMapping("")
	public List<Store> retrieveStores() {
		
		log.info("-- StoreController.retrieveStores called..");
		if (storeRepo.count() == 0) {
			throw new EntityNotFoundException("There is no Store saved.");
		}
		
		return storeRepo.findAll();
	}
	
	@PostMapping("")
	public ResponseEntity<Object> createStore(@RequestBody Store store) throws Exception {
		
		log.info("-- StoreController.createStore called..");
		Store newStore = storeService.create(store);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}")
						.buildAndExpand(newStore.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
}
