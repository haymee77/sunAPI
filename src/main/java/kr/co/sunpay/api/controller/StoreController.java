package kr.co.sunpay.api.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.Fee;
import kr.co.sunpay.api.repository.GroupRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import kr.co.sunpay.api.service.GroupService;
import kr.co.sunpay.api.service.MemberService;
import kr.co.sunpay.api.service.StoreService;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/store")
public class StoreController {

	@Autowired
	GroupRepository groupRepo;
	
	@Autowired
	StoreRepository storeRepo;
	
	@Autowired
	StoreService storeService;
	
	@Autowired
	MemberService memberService;
	
	/**
	 * memberUid 권한 하위의 모든 상점리스트 반환
	 * @param memberUid
	 * @return
	 */
	@GetMapping("/{memberUid}")
	@ApiOperation(value="상점리스트 가져오기", notes="{memberUid}의 그룹 권한으로 볼 수 있는 상점리스트 반환")
	public List<Store> retrieveStores(@PathVariable("memberUid") int memberUid) {
		
		log.info("-- StoreController.retrieveStores called..");
		if (storeRepo.count() == 0) {
			throw new EntityNotFoundException("There is no Store saved.");
		}
		
		Member member = memberService.getMember(memberUid);
		
		if (member.getStoreUid() > 0) {
			List<Store> stores = new ArrayList<Store>();
			stores.add(storeService.getStore(member.getStoreUid()));
			
			return stores;
		}
		
		return storeService.getStoresByMember(member);
	}
	
	/**
	 * memberUid 권한 하위의 상점리스트 반환
	 * @param memberUid
	 * @param uid
	 * @return
	 */
	@GetMapping("/{memberUid}/{uid}")
	@ApiOperation(value="상점 정보 가져오기", notes="{memberUid} 권한 확인 후 {uid} 상점 정보 반환")
	public Store retrieveStore(@PathVariable("memberUid") int memberUid, @PathVariable("uid") int uid) {
		
		Member member = memberService.getMember(memberUid);
		boolean find = false;
		List<Store> memberStores = storeService.getStoresByMember(member);
		
		for (Store s : memberStores) {
			if (s.getUid() == uid) {
				return s;
			}
		}
		
		if (!find) {
			throw new EntityNotFoundException("There is no available store.");
		}
		
		return null;
	}
	
	/**
	 * 상점 생성 - 생성하려는 상점의 소속 그룹이 memberUid 하위 그룹인 경우에만 생성 가능
	 * @param memberUid
	 * @param store
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/{memberUid}")
	@ApiOperation(value="상점 생성 - Dashboard", notes="{memberUid} 권한 확인 후 상점 생성, {memberUid} 하위 소속으로만 생성 가능")
	public ResponseEntity<Object> createStoreByManager(@PathVariable("memberUid") int memberUid, @RequestBody Store store) throws Exception {
		
		log.info("-- StoreController.createStoreByManager called..");
		Store newStore = storeService.create(store, memberUid);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}")
						.buildAndExpand(newStore.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	/**
	 * 상점 생성
	 * @param store
	 * @return
	 * @throws Exception
	 */
	@PostMapping("")
	@ApiOperation(value="상점 생성", notes="본사 소속으로만 생성 가능")
	public ResponseEntity<Object> createStore(@RequestBody Store store) throws Exception {
		
		log.info("-- StoreController.createStore called..");
		store.setGroup(groupRepo.findByRoleCode(GroupService.ROLE_HEAD).get());
		Store newStore = storeService.create(store);
		Member storeOwner = newStore.getMembers().get(0);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/" + storeOwner.getUid() + "/{uid}")
						.buildAndExpand(newStore.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@PutMapping("/{memberUid}/{storeUid}")
	@ApiOperation(value="상점 수정 - Dashboard", notes="{memberUid} 권한 확인 후 상점 수정, {memberUid} 하위 상점인 경우만 수정 가능")
	public ResponseEntity<Object> updateStoreByManager(@PathVariable("memberUid") int memberUid,
			@PathVariable("storeUid") int storeUid, @RequestBody Store store) throws Exception {
		
		storeService.update(storeUid, store, memberUid);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@GetMapping("/fee/{memberUid}/{groupUid}")
	@ApiOperation(value = "상점 수수료 정보", notes = "")
	public Fee getGroupFee(@ApiParam(value="멤버UID") @PathVariable(value="memberUid") int memberUid, @ApiParam(value = "상점을 생성할 그룹UID") @PathVariable(value = "groupUid") int groupUid) {

		return storeService.getFee(memberUid, groupUid);
	}
}
