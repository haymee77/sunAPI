package kr.co.sunpay.api.controller;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.co.sunpay.api.domain.Payment;
import kr.co.sunpay.api.repository.PaymentRepository;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/payment/")
public class PaymentController {
	
	@Autowired
	private PaymentRepository paymentRepo;
	
	/**
	 * 특정 상점(storeId)의 결제 시도 내역 모든 리스트 반환힘.
	 * @param storeId
	 * @return
	 */
	@GetMapping("/ks/pay/{storeId}")
	public List<Payment> retrieveAllPayment(@PathVariable String storeId) {
		
		log.info("-- PaymentController.retrieveAllPayment called...");
		
		return paymentRepo.findByStoreId(storeId);
	}
	
	/**
	 * 특정 상점(storeId)의 특정 주문건(orderNo)에 대한 결제 시도 내역 반환함.
	 * @param storeId
	 * @param orderNo
	 * @return
	 * @throws Exception 
	 */
	@GetMapping("/ks/pay/{storeId}/{orderNo}")
	public Payment retrievePayment(@PathVariable String storeId, @PathVariable String orderNo) throws Exception {
		
		log.info("-- PaymentController.retrievePayment called...");
		Optional<Payment> pay = paymentRepo.findByStoreIdAndOrderNo(storeId, orderNo);
		
		return pay.get();
	}
	
	/**
	 * 상점(storeId)의 주문건(orderNo) 삭제.
	 * @param storeId
	 * @param orderNo
	 * @return
	 */
	@DeleteMapping("/ks/pay/{storeId}/{orderNo}")
	public ResponseEntity<?> deletePayment(@PathVariable String storeId, @PathVariable String orderNo) {
		
		log.info("-- PaymentController.deletePayment called...");
		Optional<Payment> pay = paymentRepo.findByStoreIdAndOrderNo(storeId, orderNo);
		
		if (pay.isPresent()) {
			
			log.info("-- uid: " + pay.get().getUid());
			paymentRepo.deleteById(pay.get().getUid());
			return new ResponseEntity<>(HttpStatus.OK);
			
		} else {
			
			log.info("-- No order(No. " + orderNo + ", Store Id: " + storeId + ")");
			return new ResponseEntity<>(HttpStatus.GONE);
		}
	}
	
	/**
	 * 결제 시도 내역 저장 후 URL 반환.
	 * @param payment
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/ks/pay")
	public ResponseEntity<Object> createPay(@RequestBody Payment payment) throws Exception {
		
		log.info("-- PaymentController.addPay called...");
		log.info(payment.toString());
		
		Payment savedPay = paymentRepo.save(payment);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{storeId}/{orderNo}")
						.buildAndExpand(savedPay.getStoreId(), savedPay.getOrderNo()).toUri();
		
		return ResponseEntity.created(location).build();
	}
}
