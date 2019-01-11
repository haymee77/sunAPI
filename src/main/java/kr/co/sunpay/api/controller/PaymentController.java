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
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.PaymentItem;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
import kr.co.sunpay.api.repository.MemberRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import kr.co.sunpay.api.service.MemberService;
import kr.co.sunpay.api.service.StoreService;

@RestController
@RequestMapping("/payment")
public class PaymentController {

	@Autowired
	StoreRepository storeRepo;

	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;
	
	@Autowired
	StoreService storeService;
	
	@Autowired
	MemberService memberService;

	@GetMapping("/{memberUid}/{storeUid}")
	@ApiOperation(value = "결제통계 데이터 요청", notes = "검색 조건: 상점ID(필수), 기간, 결제방법, 정산방법")
	public List<PaymentItem> retrieveList(
			@ApiParam(value = "멤버UID", required = true) @PathVariable(value = "memberUid") int memberUid,
			@ApiParam(value = "상점UID", required = true) @PathVariable(value = "storeUid") int storeUid,
			@ApiParam(example = "20190101(YYYYMMDD)") @RequestParam(value = "결제날짜검색 - 시작일", required = true) String startDate,
			@ApiParam(example = "20190101(YYYYMMDD)") @RequestParam(value = "결제날짜검색 - 종료일", required = true) String endDate,
			@RequestParam(value = "결제방법", required = true) List<String> paymethod,
			@RequestParam(value = "정산방법(코드값)", required = true) List<String> serviceTypeCode) {
		
		// memberUid 가 storeId 에 대한 권힌이 있는지 확인
		Optional<Store> opStore = storeRepo.findByUid(storeUid);
		Optional<Member> opMember = memberRepo.findByUid(memberUid);

		if (!opStore.isPresent())
			throw new EntityNotFoundException("상점을 찾을 수 없습니다.");
		if (!opMember.isPresent())
			throw new EntityNotFoundException("멤버를 찾을 수 없습니다.");
		
		Store store = opStore.get();
		Member member = opMember.get();
		
		if (!memberService.hasStoreQualification(member, store)) {
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		}
		
		List<String> storeIds = new ArrayList<String>();
		store.getStoreIds().forEach(storeId -> {
			storeIds.add(storeId.getId());
		});
		
		List<KsnetPayResult> payList = ksnetPayResultRepo.findByStoreIdAndtrddtAndserviceTypeCd(storeIds, startDate, endDate, serviceTypeCode);
		
		List<PaymentItem> list = new ArrayList<PaymentItem>(); 
		
		payList.forEach(pay -> {
			if (!paymethod.contains(pay.getKsnetPay().getSndPaymethod())) {
				return;
			}
			
			PaymentItem item = new PaymentItem();
			
			item.setPaidDate(pay.getTrddt() + pay.getTrdtm());
			item.setAmount(pay.getAmt());
			item.setServiceTypeCode(pay.getServiceTypeCd());
			item.setTrNo(pay.getTrno());
			item.setPaymethodCode(pay.getKsnetPay().getSndPaymethod());
			item.setGoodsName(pay.getKsnetPay().getSndGoodname());
			item.setOrderName(pay.getKsnetPay().getSndOrdername());
			item.setOrderNo(pay.getKsnetPay().getSndOrdernumber());
			
			list.add(item);
		});

		return list;
	}
}
