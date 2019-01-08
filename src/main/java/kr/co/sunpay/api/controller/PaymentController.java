package kr.co.sunpay.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.MemberRole;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.PaymentItem;
import kr.co.sunpay.api.repository.MemberRepository;
import kr.co.sunpay.api.repository.StoreRepository;

@RestController
@RequestMapping("/payment")
public class PaymentController {
	
	@Autowired
	StoreRepository storeRepo;
	
	@Autowired
	MemberRepository memberRepo;
	
	boolean qualified;
	
	@GetMapping("/{memberUid}/{storeUid}")
	@ApiOperation(value="결제통계 데이터 요청", notes="검색 조건: 상점ID(필수), 기간, 결제방법, 정산방법")
	public List<PaymentItem> retrieveList(@ApiParam(value="멤버UID", required=true) @PathVariable(value="memberUid") int memberUid,
			@ApiParam(value="상점UID", required=true) @PathVariable(value="storeUid") int storeUid,
			@RequestParam(value="결제날짜검색 - 시작일", required=false) String startDate, 
			@RequestParam(value="결제날짜검색 - 종료일", required=false) String endDate, 
			@RequestParam(value="결제방법", required=false) String paymethod, 
			@RequestParam(value="정산방법(코드값)", required=false) String serviceTypeCode) {
		
		List<PaymentItem> list = new ArrayList<PaymentItem>();
		 qualified = false;
		
		// memberUid 가 storeId 에 대한 권힌이 있는지 확인
		Optional<Store> store = storeRepo.findByUid(storeUid);
		Optional<Member> member = memberRepo.findByUid(memberUid);
		
		if (!store.isPresent()) throw new EntityNotFoundException("상점을 찾을 수 없습니다.");
		if (!member.isPresent()) throw new EntityNotFoundException("멤버를 찾을 수 없습니다.");
		
		// 멤버 권한 확인
		List<MemberRole> roles = member.get().getRoles();
		for (MemberRole role : roles) {
			// 최고관리자, 본사 멤버의 경우 자격있음
			if (role.getRoleName().equals("TOP") || role.getRoleName().equals("HEAD")) {
				qualified = true;
				break;
			}
			
		}
		
		System.out.println(member.get().getRoles());
		
		
		
		if (member.get().getStore() != null) {
			if (member.get().getStore().getUid() == storeUid) {
				System.out.println("상점 멤버임");
			} else {
			}
		} else {
			System.out.println("상점 멤버 아님 > 대리점/지사/본사 멤버인지 확인");
		}
		
		return list;
	}
}
