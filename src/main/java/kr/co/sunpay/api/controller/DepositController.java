package kr.co.sunpay.api.controller;

import java.text.ParseException;

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
import kr.co.sunpay.api.model.DepositService;
import kr.co.sunpay.api.service.DepositMsgParser;
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
		
		// 문자메세지 파싱 
		DepositMsgParser msgParser = new DepositMsgParser(depositMessage.getMessage());
		
		// 파싱 오류 시 오류메세지 반환
		if (!msgParser.isParsingOk()) {
			apiResponseBody.setSuccess(false);
			apiResponseBody.setMessage(msgParser.getParsingMsg());
			
			log.info("-- return: " + apiResponseBody.toString());
			return new ResponseEntity<>(apiResponseBody, HttpStatus.BAD_REQUEST);
		}
		
		String depositNo = msgParser.getDepositNo();
		int amt = msgParser.getAmt();

		// 예치금 번호로 상점 확인
		if (!depositService.isValidNo(depositNo)) {
			apiResponseBody.setSuccess(true);
			apiResponseBody.setMessage("입금번호 확인중입니다.");
			depositService.writeLog(null, depositNo, null, DepositService.TYPE_DEPOSIT, null, DepositService.STATUS_WAITING, amt);
		} else {
			depositService.deposit(depositNo, amt);
			apiResponseBody.setSuccess(true);
			apiResponseBody.setMessage("입금번호: " + depositNo + ", 입금액: " + amt + ", 예치금 증액 완료");
		}
		
		log.info("-- return: " + apiResponseBody.toString());
		return new ResponseEntity<>(apiResponseBody, HttpStatus.OK);
	}
}
