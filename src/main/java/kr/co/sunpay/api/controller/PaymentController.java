package kr.co.sunpay.api.controller;

import java.util.List;

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
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.PaymentItem;
import kr.co.sunpay.api.service.PaymentService;
import kr.co.sunpay.api.service.StoreService;

@RestController
@RequestMapping("/payment")
public class PaymentController {
	
	@Autowired
	PaymentService paymentService;
	
	@Autowired
	StoreService storeService;

	@GetMapping("/{memberUid}/{storeUid}")
	@ApiOperation(value = "결제통계 데이터 요청", notes = "검색 조건: 상점ID(필수), 기간, 결제방법, 정산방법")
	public List<PaymentItem> retrieveList(
			@ApiParam(value = "멤버UID", required = true) @PathVariable(value = "memberUid") int memberUid,
			@ApiParam(value = "상점UID", required = true) @PathVariable(value = "storeUid") int storeUid,
			@ApiParam(value = "검색기간 - 시작일(YYYYMMDD)", required = true) @RequestParam(value = "startDate", required = true) String startDate,
			@ApiParam(value = "검색기간 - 종료일(YYYYMMDD)", required = true) @RequestParam(value = "endDate", required = true) String endDate,
			@ApiParam(value = "결제수단(코드값)", required = true) @RequestParam(value = "paymethod", required = true) List<String> paymethods,
			@ApiParam(value = "서비스 타입(순간정산, D2 등 코드값)", required = true) @RequestParam(value = "serviceTypeCode", required = true) List<String> serviceTypeCodes) {
		
		Store store = storeService.getStore(storeUid);
		Member member = storeService.getMember(memberUid);
		
		// 파라미터 Null 체크
		if (member == null)
			throw new EntityNotFoundException("멤버를 찾을 수 없습니다.");
		
		if (store == null)
			throw new EntityNotFoundException("상점을 찾을 수 없습니다.");
		
		// 멤버 권한 확인
		if (!storeService.hasStoreQualification(member, store))
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		
		return paymentService.getPaymentItems(store, startDate, endDate, paymethods, serviceTypeCodes);
	}
}
