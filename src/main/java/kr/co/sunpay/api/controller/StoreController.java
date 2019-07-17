package kr.co.sunpay.api.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.MemberResponse;
import kr.co.sunpay.api.model.StoreRequest;
import kr.co.sunpay.api.repository.GroupRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import kr.co.sunpay.api.service.GroupService;
import kr.co.sunpay.api.service.MemberService;
import kr.co.sunpay.api.service.StoreService;
import kr.co.sunpay.api.util.Sunpay;

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
	GroupService groupService;

	@Autowired
	MemberService memberService;

	/**
	 * memberUid 권한 하위의 모든 상점리스트 반환
	 * 
	 * @param memberUid
	 * @return
	 */
	@GetMapping("/{memberUid}")
	@ApiOperation(value = "상점리스트 가져오기", notes = "{memberUid}의 그룹 권한으로 볼 수 있는 상점리스트 반환")
	public List<Store> retrieveStores(@PathVariable("memberUid") int memberUid) {

		Member member = memberService.getMember(memberUid);
		List<Store> stores = new ArrayList<Store>();

		if (!Sunpay.isEmpty(member.getStore())) {
			stores.add(member.getStore());
		} else {
			stores = storeService.getStoresByMember(member);
		}

		// 그룹별 수수료 정보 합산하여 리턴함
		for (Store s : stores) {
			s = s.hideFee();
		}

		return stores;
	}

	/**
	 * memberUid 권한 하위의 상점리스트 반환
	 * 
	 * @param memberUid
	 * @param uid
	 * @return
	 */
	@GetMapping("/{memberUid}/{uid}")
	@ApiOperation(value = "상점 정보 가져오기", notes = "{memberUid} 권한 확인 후 {uid} 상점 정보 반환")
	public Store retrieveStore(@PathVariable("memberUid") int memberUid, @PathVariable("uid") int uid) {

		Member member = memberService.getMember(memberUid);
		boolean find = false;
		List<Store> memberStores = storeService.getStoresByMember(member);

		for (Store s : memberStores) {
			if (s.getUid() == uid) {
				//return s.hideFee();
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
	 * 
	 * @param memberUid
	 * @param store
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/old/{memberUid}")
	@ApiOperation(value = "상점 생성 - Dashboard", notes = "{memberUid} 권한 확인 후 상점 생성, {memberUid} 하위 소속으로만 생성 가능")
	public ResponseEntity<Object> createStoreByManager(@PathVariable("memberUid") int memberUid,
			@RequestBody Store store) throws Exception {

		Store newStore = storeService.create(store, memberUid);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}").buildAndExpand(newStore.getUid())
				.toUri();

		return ResponseEntity.created(location).build();
	}

	/**
	 * StoreRequest 로 상점 생성 - 매니저가 상점 생성 할 때
	 * 
	 * @param storeRequest
	 * @return
	 */
	@PostMapping("/{memberUid}")
	@ApiOperation(value = "대시보드에서 상점등록", notes = "{memberUid} 권한 확인 후 상점 생성, {memberUid} 하위 소속으로만 생성 가능")
	public ResponseEntity<Object> registByManager(@PathVariable("memberUid") int memberUid, @RequestBody @Valid StoreRequest storeRequest) {

		Store store = storeService.regist(storeRequest, memberUid);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}").buildAndExpand(store.getUid())
				.toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@DeleteMapping("/{uid}")
	@ApiOperation(value=" 상점 삭제 요청", notes="{uid}  상점 삭제")
	public void deleteMember(@ApiParam("상점 uid") @PathVariable int uid) {
		
		storeService.deleteStore(uid);
		
		return;
	}
	
	/**
	 * StoreRequest 로 상점 생성
	 * 
	 * @param storeRequest
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ApiOperation(value = "본사소속 상점 등록", notes = "groupUid 는 무시됨")
	public ResponseEntity<Object> regist(@RequestBody @Valid StoreRequest storeRequest) {
		
		// storeRequest 의 groupUid에 본사의 groupUid를 삽입
		storeRequest.setGroupUid(groupService.findHead().getUid());

		Store store = storeService.regist(storeRequest);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{memUid}/{uid}")
				.buildAndExpand(store.getMembers().get(0).getUid(), store.getUid()).toUri();

		return ResponseEntity.created(location).build();
	}

	@PutMapping("/{memberUid}/{storeUid}")
	@ApiOperation(value = "상점 수정 - Dashboard", notes = "{memberUid} 권한 확인 후 상점 수정, {memberUid} 하위 상점인 경우만 수정 가능")
	public ResponseEntity<Object> updateStoreByManager(@PathVariable("memberUid") int memberUid,
			@PathVariable("storeUid") int storeUid, @RequestBody Store store) throws Exception {

		if (!storeService.hasStoreQualification(memberUid, storeUid)) {
			throw new IllegalArgumentException("memberUid의 권한으로 수정할 수 없는 그룹 소속입니다.");
		}

		storeService.update(storeUid, store, memberUid);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();

		return ResponseEntity.created(location).build();
	}
	
	@GetMapping("/owner/{storeUid}")
	@ApiOperation(value = "상점 담당자 정보 반환", notes = "")
	public MemberResponse findOwner(@PathVariable("storeUid") int storeUid) {
		
		Store store = storeService.getStore(storeUid);
		if (!Sunpay.isEmpty(store)) {
			return storeService.findOwner(store);
		}
		
		return null;
	}
}
