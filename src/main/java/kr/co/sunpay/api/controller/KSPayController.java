package kr.co.sunpay.api.controller;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.sunpay.api.model.KSPayCancelBody;
import kr.co.sunpay.api.model.KSPayCancelReturns;
import kr.co.sunpay.api.service.KSPayService;

@RestController
@RequestMapping("/kspay")
public class KSPayController {
	
	@Autowired
	KSPayService kspayService;

	@PostMapping("/cancel")
	public ResponseEntity<Object> cancel(@RequestBody KSPayCancelBody cancelBody) {
		
		System.out.println("-- /kspay/cancel start");
		KSPayCancelReturns result = new KSPayCancelReturns("", "X", "", "", "취소거절", "");
		
		// 결제 취소 요청 저장
		kspayService.saveCancelRequest(cancelBody);
		
		// 예치금 확인 및 차감
		String storeId = cancelBody.getStoreid();
		int refundAmt = kspayService.getPaidAmt(cancelBody.getTrno());
		
		if (kspayService.checkDeposit(storeId, refundAmt)) {
			
			kspayService.deductDeposit(storeId, refundAmt);
			
		} else {
			
			// 예치금 부족 시 통신 종료 
			result.setRMessage2("상점 취소예치금 금액 부족");
			return new ResponseEntity<Object>(result, HttpStatus.FOUND);
		}
		
		// KSPay 통신 시작
		result = kspayService.sendKSPay(cancelBody);
		
		// 통신 결과에 문제있는 경우 보증금 원복
		if (result.getRStatus().equals("X")) {
			kspayService.resetDeposit(storeId, refundAmt);
		}
		
		return new ResponseEntity<Object>(result, HttpStatus.FOUND);
	}
}
