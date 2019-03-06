package kr.co.sunpay.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import kr.co.sunpay.api.domain.ContactOto;
import kr.co.sunpay.api.service.ContactOtoService;

/**
 * 고객센터 > 1:1 문의 API 
 * @author himeepark
 *
 */

@RestController
@RequestMapping("/contact/oto")
public class ContactOtoController {
	
	@Autowired
	private ContactOtoService contactOtoService;
	
	@GetMapping("")
	public List<ContactOto> retrieveContactOto() {
		
		return null;
	}
	
	@PostMapping("")
	@ApiOperation(value="1:1 문의 등록")
	public ResponseEntity<Object> createContactOto(@RequestBody ContactOto contactOto) {
		
		try {
			contactOtoService.registValidator(contactOto);
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		
		ContactOto newContactOto = contactOtoService.regist(contactOto);
		
		if (newContactOto == null) {
			throw new IllegalArgumentException("Cannot regist inquery.");
		}
		
		return ResponseEntity.ok().build();
	}
}
