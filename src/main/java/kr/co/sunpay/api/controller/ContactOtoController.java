package kr.co.sunpay.api.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import kr.co.sunpay.api.domain.ContactOto;
import kr.co.sunpay.api.model.ContactOtoAnswerRequest;
import kr.co.sunpay.api.model.ContactOtoRequest;
import kr.co.sunpay.api.model.ContactOtoResponse;
import kr.co.sunpay.api.service.ContactOtoService;

/**
 * 고객센터 > 1:1 문의 API
 * 
 * @author himeepark
 *
 */

@RestController
@RequestMapping("/contact/oto")
public class ContactOtoController {

	@Autowired
	private ContactOtoService contactOtoService;

	@GetMapping("/{uid}")
	@ApiOperation(value = "1:1 문의 리턴")
	public ContactOtoResponse retrieveContactOto(@PathVariable(value = "uid") int uid) {

		return contactOtoService.findByUid(uid);
	}

	@GetMapping("")
	@ApiOperation(value = "1:1 문의 리스트 리턴")
	public List<ContactOtoResponse> retrieveContactOto(
			@ApiParam(name = "sDate", value = "Format: YYYY-MM-DD", required = true) @RequestParam(value = "sDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sDate,
			@ApiParam(name = "eDate", value = "Format: YYYY-MM-DD", required = true) @RequestParam(name = "eDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eDate,
			@ApiParam(name = "writer", value = "작성자", required = false) @RequestParam(name = "writer", required = false) String writer,
			@ApiParam(name = "type", value = "문의유형", required = false) @RequestParam(name = "type", required = false) String typeCode) {

		return contactOtoService.findFiltering(sDate, eDate, writer, typeCode);
	}

	@PostMapping("")
	@ApiOperation(value = "1:1 문의 등록")
	public ResponseEntity<Object> createContactOto(@RequestBody @Valid ContactOtoRequest contactOtoRequest) {

		ContactOto newContactOto = contactOtoService.regist(contactOtoRequest);

		if (newContactOto == null) {
			throw new IllegalArgumentException("Cannot regist inquery.");
		}

		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/answer/{memberUid}/{uid}")
	@ApiOperation(value="1:1 문의 답변, 성공 시 자동 메일 발송")
	public ResponseEntity<Object> registAnswer(
			@ApiParam("작성자 UID") @PathVariable(value="memberUid") int memberUid,
			@ApiParam("문의글 UID") @PathVariable(value="uid") int uid,
			@RequestBody @Valid ContactOtoAnswerRequest contactOtoAnswerRequest) {
		
		contactOtoService.updateAnswer(memberUid, uid, contactOtoAnswerRequest);
		
		return ResponseEntity.ok().build();
	}
}
