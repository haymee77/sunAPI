package kr.co.sunpay.api.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import kr.co.sunpay.api.domain.Payment;
import kr.co.sunpay.api.repository.PaymentRepository;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/payment/")
public class PaymentController {

	@Autowired
	private PaymentRepository paymentRepo;

	@GetMapping("/ks/pay/{storeId}")
	@ApiOperation(value = "상점ID로 결제내역 요청하기", notes = "특정 상점(storeId)의 결제 시도 내역 모든 리스트 반환함.")
	public List<Payment> retrieveAllPayment(@PathVariable String storeId) {

		log.info("-- PaymentController.retrieveAllPayment called...");

		return paymentRepo.findByStoreId(storeId);
	}

	@GetMapping("/ks/pay/{storeId}/{orderNo}")
	@ApiOperation(value = "상점ID와 주문번호로 결제내역 요청하기", notes = "특정 상점(storeId)의 특정 주문건(orderNo)에 대한 결제 시도 내역 반환함.")
	public Payment retrievePayment(@PathVariable String storeId, @PathVariable String orderNo) throws Exception {

		log.info("-- PaymentController.retrievePayment called...");
		Optional<Payment> pay = paymentRepo.findByStoreIdAndOrderNo(storeId, orderNo);

		if (!pay.isPresent())
			throw new EntityNotFoundException("storeId: " + storeId + " | orderNo: " + orderNo);

		return pay.get();
	}

	@DeleteMapping("/ks/pay/{storeId}/{orderNo}")
	@ApiOperation(value = "상점ID와 주문번호로 결제내역 삭제", notes = "상점(storeId)의 주문건(orderNo) 삭제.")
	public void deletePayment(@PathVariable String storeId, @PathVariable String orderNo) {

		log.info("-- PaymentController.deletePayment called...");
		Optional<Payment> pay = paymentRepo.findByStoreIdAndOrderNo(storeId, orderNo);

		if (!pay.isPresent())
			throw new EntityNotFoundException("No payment exist.(storeId: " + storeId + ", orderNo: " + orderNo + ")");

		paymentRepo.deleteById(pay.get().getUid());
	}

	@PostMapping("/ks/pay")
	@ApiOperation(value = "결제 시도 내역 저장하기", notes = "결제 시도 내역 저장 후 URL 반환.")
	public ResponseEntity<Object> createPay(@RequestBody Payment payment) throws Exception {

		log.info("-- PaymentController.addPay called...");
		log.info(payment.toString());

		// TODO: storeId validation check.

		Payment savedPay = paymentRepo.save(payment);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{storeId}/{orderNo}")
				.buildAndExpand(savedPay.getStoreId(), savedPay.getOrderNo()).toUri();

		return ResponseEntity.created(location).build();
	}

	@PutMapping("/ks/pay/{uid}")
	@ApiOperation(value = "결제내역 수정하기", notes = "결제 내역(uid) 수정")
	public ResponseEntity<Object> updatePay(@RequestBody Payment payment, @PathVariable int uid) {

		log.info("-- PaymentController.updatePay...");
		log.info(payment.toString());

		Optional<Payment> existPay = paymentRepo.findByUid(uid);

		if (!existPay.isPresent())
			throw new EntityNotFoundException("No payment exist.(uid: " + uid + ")");

		payment.setUid(uid);
		paymentRepo.save(payment);

		return ResponseEntity.noContent().build();
	}
}
