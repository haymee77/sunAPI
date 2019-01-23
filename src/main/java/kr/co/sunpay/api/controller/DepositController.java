package kr.co.sunpay.api.controller;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import kr.co.sunpay.api.model.ApiResponseBody;
import kr.co.sunpay.api.model.DepositMessage;
import kr.co.sunpay.api.service.DepositService;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/deposit")
public class DepositController {
	
	@Autowired
	DepositService depositService;

	@PostMapping("/message")
	@ApiOperation(value = "취소예치금 입금 문자 전송", notes = "취소예치금 입금 문자 받아서 파싱하여 DB연동")
	public ResponseEntity<Object> insertDeposit(@RequestBody DepositMessage depositMessage) throws ParseException {

		log.info("-- DepositController.insertDeposit called..");
		ApiResponseBody apiResponseBody = new ApiResponseBody();
		String message = depositMessage.getMessage();
		String lines[] = message.split("\n");
		
		// 국민은행 기준 문자메세지 7줄 체크 
		if (lines.length < 7) {
			apiResponseBody.setSuccess(false);
			apiResponseBody.setMessage("Message format error");
			
			log.info("-- return: " + apiResponseBody.toString());
			return new ResponseEntity<>(apiResponseBody, HttpStatus.BAD_REQUEST);
		}
		
		// 국민은행 기준 4번째 줄에 입금처, 6번째 줄에 입금액 확인
		String depositNo = lines[3];
		String depositAmount = lines[5];
		
		// 입금처(SP_STORES.DEPOSIT_NO) 번호는 6자여야함
		if (depositNo.length() != 6) {
			apiResponseBody.setSuccess(false);
			apiResponseBody.setMessage("Deposit Number is invalid");
			
			log.info("-- return: " + apiResponseBody.toString());
			return new ResponseEntity<>(apiResponseBody, HttpStatus.BAD_REQUEST);
			
		}

		// 예치금 번호로 상점 확인
		if (!depositService.isValidNo(depositNo)) {
			apiResponseBody.setSuccess(false);
			apiResponseBody.setMessage("Can not find Store");
			
			return new ResponseEntity<>(apiResponseBody, HttpStatus.BAD_REQUEST);
		};
		
		depositService.deposit(depositNo, NumberFormat.getNumberInstance(Locale.UK).parse(depositAmount).intValue());
		apiResponseBody.setSuccess(true);
		apiResponseBody.setMessage("KB은행, 입금번호: " + depositNo + ", 입금액: " + depositAmount + ", 예치금 증액 완료");
		
		log.info("-- return: " + apiResponseBody.toString());
		return new ResponseEntity<>(apiResponseBody, HttpStatus.OK);
	}
}
