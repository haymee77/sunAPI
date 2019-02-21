package kr.co.sunpay.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.sunpay.api.domain.KsnetRefundLog;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.model.DepositService;
import kr.co.sunpay.api.model.KsnetRefundBody;
import kr.co.sunpay.api.model.KspayRefundReturns;
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

	@PostMapping("/refund")
	public ResponseEntity<Object> refund(@RequestBody KsnetRefundBody refund) {
		
		System.out.println("-- /kspay/refund start");
		KspayRefundReturns result = new KspayRefundReturns("", "X", "", "", "취소거절", "");

		// 결제 취소 요청 저장
		KsnetRefundLog log = ksnetService.saveRefundLog(refund);
		
		try {
			// 주문정보 조회 
			KsnetPayResult paidResult = ksnetService.getPaidResult(refund.getTrno(), refund.getStoreid());
			if (paidResult == null) {
				result.setRMessage2("주문정보 없음");
				ksnetService.updateRefundLog(log, result);
				return new ResponseEntity<Object>(result, HttpStatus.FOUND);
			}
			
			if (ksnetService.hasCancelSuccessLog(refund)) {
				result.setRMessage2("기취소거래건");
				ksnetService.updateRefundLog(log, result);
				return new ResponseEntity<Object>(result, HttpStatus.FOUND);
			}
			
			boolean isInstantPaid = paidResult.getServiceTypeCd().equals("INSTANT") ? true : false;
			log.setAmt(paidResult.getAmt());

			// 순간결제 이용중이고 카드결제건의 취소요청 시 예치금 확인 및 차감
			if (isInstantPaid && refund.getAuthty().equals(KsnetService.KSPAY_AUTHTY_CREDIT)) {
				try {
					depositService.tryRefund(refund.getStoreid(), paidResult);
				} catch (Exception ex) {
					// 예치금 부족시 PUSH알림 및 통신 종료 
					depositService.pushRefundLack(refund.getStoreid(), paidResult);
					
					result.setRMessage2(ex.getMessage());
					ksnetService.updateRefundLog(log, result);
					return new ResponseEntity<Object>(result, HttpStatus.FOUND);
				}
				
			} 

			// KSPay 통신 시작
			result = ksnetService.sendKSPay(refund);
			
			// 순간정산 환불 시 처리
			if (isInstantPaid) {
				// KSPay 통신 성공, 환불 완료 처리
				if (result.getRStatus().equals("O")) {
					System.out.println("-- 통신 성공");
					// 임시 차감된 예치금의 상태를 완료로 업데이트
					depositService.completeRefund(refund);
					
				// 통신 결과에 문제가 있는 경우
				} else {
					// 예치금 원복
					depositService.resetDeposit(refund);
				}
			}
			
			ksnetService.updateRefundLog(log, result);
			
			// 취소 결과 PUSH 발송
			pushService.sendPush(log);
			
			return new ResponseEntity<Object>(result, HttpStatus.FOUND);
			
		} catch (Exception e) {
			result.setRMessage2("취소할 수 없는 주문건입니다.");
			ksnetService.updateRefundLog(log, result);
			return new ResponseEntity<Object>(result, HttpStatus.FOUND);
		}
		
	}
}
