package kr.co.sunpay.api.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		boolean isKb = depositMessage.getMessage().indexOf("[KB]") != -1 ? true : false;
		boolean isShinhan = message.indexOf("신한") != -1 ? true : false;

		String lines[] = message.split("\n");

		if (isKb && lines.length > 5) {

			apiResponseBody.setSuccess(true);
			apiResponseBody.setMessage("KB은행, 입금번호: " + lines[3] + ", 입금액: " + lines[5]);

		} else if (isShinhan && lines.length > 4) {

			String amount = "0";
			Pattern pattern = Pattern.compile("(([0-9]{0,3}+,){0,}[0-9]{0,3}$)");
			Matcher matcher = pattern.matcher(lines[3]);
			
			if (matcher.find()) {
				amount = matcher.group(1);
			}
			
			apiResponseBody.setSuccess(true);
			apiResponseBody.setMessage("신한은행, 입금번호: " + lines[4] + ", 입금액: " + amount);
			
		} else {
			
			apiResponseBody.setSuccess(false);
			apiResponseBody.setMessage("정상적인 메세지가 아닙니다.");
		}

		return new ResponseEntity<>(apiResponseBody, HttpStatus.OK);
	}
}
