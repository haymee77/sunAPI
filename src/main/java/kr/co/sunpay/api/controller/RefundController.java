package kr.co.sunpay.api.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.model.RefundItemResponse;
import kr.co.sunpay.api.service.RefundService;
import kr.co.sunpay.api.util.Sunpay;

@RestController
@RequestMapping("/refund")
public class RefundController {
	
	@Autowired
	RefundService refundService;

	@GetMapping("/{memberUid}/{storeUid}")
	@ApiOperation(value = "환불 데이터 요청(환불 성공건만 반환)", notes = "검색 조건: 상점ID(필수), 기간, 결제방법, 정산방법")
	public List<RefundItemResponse> retrieveRefunds(@ApiParam(value = "멤버UID", required = true) @PathVariable(value = "memberUid") int memberUid,
			@ApiParam(value = "상점UID", required = true) @PathVariable(value = "storeUid") int storeUid,
			@ApiParam(value = "검색기간 - 시작일(YYYY-MM-DD)", required = true) @RequestParam(value = "startDate", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sDate,
			@ApiParam(value = "검색기간 - 종료일(YYYY-MM-DD)", required = true) @RequestParam(value = "endDate", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eDate,
			@ApiParam(value = "결제수단(코드값)", required = false) @RequestParam(value = "paymethod", required = false) List<String> paymethodCodes,
			@ApiParam(value = "서비스 타입(순간정산, D2 등 코드값)", required = false) @RequestParam(value = "serviceTypeCode", required = false) List<String> serviceTypeCode) {
		
		// Input data validation check
		// 1. 멤버 조회
		if (Sunpay.isEmpty(refundService.getMember(memberUid)))
			throw new EntityNotFoundException("멤버를 찾을 수 없습니다.");
		
		// 2. 상점 조회
		if (Sunpay.isEmpty(refundService.getStore(storeUid)))
			throw new EntityNotFoundException("상점을 찾을 수 없습니다.");
		
		// 3. 날짜 변환(LocalDate > LocalDateTime) 및 시작일, 종료일 검사(90일 이내 검색 가능)
		LocalDateTime sDateTime = sDate.atStartOfDay();
		LocalDateTime eDateTime = eDate.atTime(23, 59, 59);

		// 시작일, 종료일 검사
		if (sDateTime.isAfter(eDateTime))
			throw new IllegalArgumentException("종료일이 시작일보다 먼저일 수 없습니다.");

		// 시작일 - 종료일이 90일 이내인지 검사
		if (ChronoUnit.DAYS.between(sDateTime, eDateTime) > 90)
			throw new IllegalArgumentException("검색기간은 90일이내만 가능합니다.");
		
		
		// 4. 권한 확인
		if (!refundService.hasStoreQualification(memberUid, storeUid)) 
			throw new BadCredentialsException("상점 조회 권한이 없습니다.");
		
		return refundService.getRefundItems(storeUid, sDateTime, eDateTime, paymethodCodes, serviceTypeCode);
	}
}
