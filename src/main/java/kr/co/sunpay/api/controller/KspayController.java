package kr.co.sunpay.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.sunpay.api.domain.KspayCancelLog;
import kr.co.sunpay.api.model.DepositService;
import kr.co.sunpay.api.model.KSPayCancelBody;
import kr.co.sunpay.api.model.KSPayCancelReturns;
import kr.co.sunpay.api.service.KspayService;
import kr.co.sunpay.api.service.StoreService;

@RestController
@RequestMapping("/kspay")
public class KspayController {
	
	@Autowired
	KspayService kspayService;
	
	@Autowired
	DepositService depositService;
	
	@Autowired
	StoreService storeService;

	@PostMapping("/cancel")
	public ResponseEntity<Object> cancel(@RequestBody KSPayCancelBody cancelBody) {
		
		System.out.println("-- /kspay/cancel start");
		KSPayCancelReturns result = new KSPayCancelReturns("", "X", "", "", "취소거절", "");
		
		// 결제 취소 요청 저장
		KspayCancelLog log = kspayService.saveCancelLog(cancelBody);
		
		boolean isInstantOn = storeService.isInstantOn(cancelBody.getStoreid());
		
		// 순간결제라면 예치금 확인 및 차감
		if (isInstantOn) {
			
			try {
				depositService.tryRefund(cancelBody);
			} catch (Exception ex) {
				// 예치금 부족 시 통신 종료 
				result.setRMessage2(ex.getMessage());
				kspayService.updateCancelLog(log, result);
				return new ResponseEntity<Object>(result, HttpStatus.FOUND);
			}
			
		} 
		
		// KSPay 통신 시작
		result = kspayService.sendKSPay(cancelBody);
		
		// 순간정산 사용중이고 통신 결과에 문제있는 경우 보증금 원복
		if (isInstantOn && result.getRStatus().equals("X")) {
			depositService.resetDeposit(cancelBody);
		}
		
		kspayService.updateCancelLog(log, result);
		return new ResponseEntity<Object>(result, HttpStatus.FOUND);
	}
}
