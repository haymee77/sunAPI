package kr.co.sunpay.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.KsnetRefundLog;
import kr.co.sunpay.api.model.DepositService;
import kr.co.sunpay.api.model.KsnetRefundBody;
import kr.co.sunpay.api.model.KspayRefundReturns;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
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
	
	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;

	@PostMapping("/refund")
	public ResponseEntity<Object> refund(@RequestBody KsnetRefundBody refund) {
		
		System.out.println("-- /kspay/refund start");
		System.out.println(refund);
		KspayRefundReturns result;
		String storeId=ksnetPayResultRepo.findByTrnoAndAuthyn(refund.getTrno(), "O").get().getStoreId();
		refund.setStoreid(storeId);
		try {
			result = ksnetService.refund(refund);
			return new ResponseEntity<Object>(result, HttpStatus.FOUND);
			
		} catch (Exception e) {
			result = new KspayRefundReturns(refund.getTrno(), "X", "", "", "취소거절", "취소할 수 없는 주문건입니다.");
			
			// 환불요청정보 저장, 결과 업데이트
			
			KsnetRefundLog log = ksnetService.saveRefundLog(refund);
			log.setStatusCode(KsnetRefundLog.STATUS_ERROR);
			ksnetService.updateRefundLog(log, result);
			return new ResponseEntity<Object>(result, HttpStatus.FOUND);
		}
		
	}
}
