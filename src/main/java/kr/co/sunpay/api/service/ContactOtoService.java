package kr.co.sunpay.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.ContactOto;
import kr.co.sunpay.api.repository.ContactOtoRepository;

@Service
public class ContactOtoService {
	
	public final static String STATUS_WAITING = "WAITING";
	public final static String STATUS_FINISH = "FINISH";
	
	@Autowired
	private ContactOtoRepository contactOtoRepo;

	public ContactOto regist(ContactOto contactOto) {
	
		if (!registValidator(contactOto)) return null;
		
		// Default 정보 설정
		contactOto.setAnswer("");
		contactOto.setStatusCode(STATUS_WAITING);
		
		return contactOtoRepo.save(contactOto);
	}
	
	public boolean registValidator(ContactOto contactOto) {
		
		// Company 검사
		// Contact 검사
		// Duty 검사
		// Mail 검사
		// Query 검사
		// Title 검사
		// TypeCode 검사
		// URL 검사
		// Writer 검사
		
		return true;
	}
}
