package kr.co.sunpay.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.sunpay.api.domain.KsnetCancelLog;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.model.DepositService;
import kr.co.sunpay.api.model.KspayCancelBody;
import kr.co.sunpay.api.model.KspayCancelReturns;
import kr.co.sunpay.api.service.KsnetService;
import kr.co.sunpay.api.service.PushService;
import kr.co.sunpay.api.service.StoreService;

@RestController
@RequestMapping("/kspay")
public class KsnetController {
	
	@Autowired
	KsnetService ksnetService;
	
	@Autowired
	DepositService depositService;
	
	@Autowired
	StoreService storeService;
	
	@Autowired
	PushService pushService;

	@PostMapping("/cancel")
	public ResponseEntity<Object> cancel(@RequestBody KspayCancelBody cancel) throws Exception {
		
		System.out.println("-- /kspay/cancel start");
		KspayCancelReturns result = new KspayCancelReturns("", "X", "", "", "취소거절", "");

		// 결제 취소 요청 저장
		KsnetCancelLog log = ksnetService.saveCancelLog(cancel);
		boolean isInstantOn = storeService.isInstantOn(cancel.getStoreid());
		
		// 주문정보 조회 
		KsnetPayResult paidResult = ksnetService.getPaidResult(cancel.getTrno(), cancel.getStoreid());
		if (paidResult == null) {
			result.setRMessage2("주문정보 없음");
			ksnetService.updateCancelLog(log, result);
			return new ResponseEntity<Object>(result, HttpStatus.FOUND);
		}
		
		log.setAmt(paidResult.getAmt());
		
		// 기취소건인지 확인 TODO: hasCancelSuccessLog 로직 구현
		if (ksnetService.hasCancelSuccessLog(cancel)) {
			result.setRMessage2("기취소거래건");
			ksnetService.updateCancelLog(log, result);
			return new ResponseEntity<Object>(result, HttpStatus.FOUND);
		}

		// 순간결제 이용중이고 카드결제건의 취소요청 시 예치금 확인 및 차감
		if (isInstantOn && cancel.getAuthty().equals(KsnetService.KSPAY_AUTHTY_CREDIT)) {
			try {
				depositService.tryRefund(cancel);
			} catch (Exception ex) {
				// 예치금 부족 시 통신 종료 
				result.setRMessage2(ex.getMessage());
				ksnetService.updateCancelLog(log, result);
				return new ResponseEntity<Object>(result, HttpStatus.FOUND);
			}
			
		} 

		// KSPay 통신 시작
		result = ksnetService.sendKSPay(cancel);
		
		// 순간정산 환불 시 처리
		if (isInstantOn) {
			// KSPay 통신 성공, 환불 완료 처리
			if (result.getRStatus().equals("O")) {
				System.out.println("-- 통신 성공");
				// 임시 차감된 예치금의 상태를 완료로 업데이트
				depositService.completeRefund(cancel);
				
			// 통신 결과에 문제가 있는 경우
			} else {
				// 예치금 원복
				depositService.resetDeposit(cancel);
			}
		}
		
		ksnetService.updateCancelLog(log, result);
		
		// 취소 결과 PUSH 발송
		pushService.sendPush(log);
		
		return new ResponseEntity<Object>(result, HttpStatus.FOUND);
	}
}
