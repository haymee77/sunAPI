package kr.co.sunpay.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import kr.co.sunpay.api.model.ApiResponseBody;
import kr.co.sunpay.api.model.DepositMessage;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/deposit")
public class DepositController {

	@PostMapping("/message")
	@ApiOperation(value = "취소예치금 입금 문자 전송", notes = "취소예치금 입금 문자 받아서 파싱하여 DB연동")
	public ResponseEntity<Object> insertDeposit(@RequestBody DepositMessage depositMessage) {

		log.info("-- DepositController.insertDeposit called..");
		ApiResponseBody apiResponseBody = new ApiResponseBody();
		String message = depositMessage.getMessage();
		String lines[] = message.split("\n");
		
		if (lines.length < 7) {
			apiResponseBody.setSuccess(false);
			apiResponseBody.setMessage("Message format error");
			
			return new ResponseEntity<>(apiResponseBody, HttpStatus.BAD_REQUEST);
		}
		
		String depositFrom = lines[3];
		String depositAmount = lines[5];
		
		if (depositFrom.length() != 6) {
			apiResponseBody.setSuccess(false);
			apiResponseBody.setMessage("Deposit Number is invalid");
			
			return new ResponseEntity<>(apiResponseBody, HttpStatus.BAD_REQUEST);
			
		} else {
			apiResponseBody.setSuccess(true);
			apiResponseBody.setMessage("KB은행, 입금번호: " + depositFrom + ", 입금액: " + depositAmount);
		}
		
		return new ResponseEntity<>(apiResponseBody, HttpStatus.OK);
	}
}
