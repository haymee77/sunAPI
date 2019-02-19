package kr.co.sunpay.api.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.DepositLog;
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
			apiResponseBody.setMessage("상점을 찾을 수 없습니다. 입금번호 확인해주세요.");
			depositService.writeLog(null, depositNo, null, DepositService.TYPE_DEPOSIT, null, DepositService.STATUS_WAITING, amt);
		} else {
			depositService.deposit(depositNo, amt);
			apiResponseBody.setSuccess(true);
			apiResponseBody.setMessage("입금번호: " + depositNo + ", 입금액: " + amt + ", 예치금 증액 완료");
			
			// TODO: 상점관리자에 예치금 입금완료 PUSH
		}
		
		log.info("-- return: " + apiResponseBody.toString());
		return new ResponseEntity<>(apiResponseBody, HttpStatus.OK);
	}
	
	@GetMapping("/{memberUid}/{depositNo}")
	@ApiOperation(value="예치금 내역", notes="예치금 번호, 타입에 대한 충전/사용 내역 리턴")
	public List<DepositLog> getDepositLogs(
			@PathVariable(value="memberUid") int memberUid, 
			@PathVariable(value="depositNo") String depositNo,
			@RequestParam(value="type", required=false) String type) {

		return depositService.getLogs(memberUid, depositNo, type);
	}
}
