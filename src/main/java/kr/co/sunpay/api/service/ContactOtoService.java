package kr.co.sunpay.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.sunpay.api.domain.ContactOto;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.model.ContactOtoAnswerRequest;
import kr.co.sunpay.api.model.ContactOtoRequest;
import kr.co.sunpay.api.model.ContactOtoResponse;
import kr.co.sunpay.api.repository.ContactOtoRepository;
import kr.co.sunpay.api.util.Sunpay;

@Service
public class ContactOtoService {
	
	public final static String STATUS_WAITING = "WAITING";
	public final static String STATUS_FINISH = "FINISH";
	
	@Autowired
	private ContactOtoRepository contactOtoRepo;
	
	@Autowired
	private MemberService memberService;

	@Transactional
	public ContactOto regist(ContactOtoRequest contactOtoRequest) {
	
		// Default 정보 설정
		contactOtoRequest.setAnswer("");
		contactOtoRequest.setStatusCode(STATUS_WAITING);
		
		return contactOtoRepo.save(contactOtoRequest.toEntity());
	}
	
	@Transactional(readOnly=true)
	public ContactOtoResponse findByUid(int uid) {
		
		ContactOtoResponse response = contactOtoRepo.findByUid(uid).map(ContactOtoResponse::new).orElse(null);
		
		if (response == null) throw new EntityNotFoundException("Not Found."); 
		
		return response; 
	}
	
	@Transactional(readOnly=true)
	public List<ContactOtoResponse> findFiltering(LocalDate sDate, LocalDate eDate, String writer, String typeCode) {

		// 날짜 변환(LocalDate > LocalDateTime)
		LocalDateTime sDateTime = sDate.atStartOfDay();
		LocalDateTime eDateTime = eDate.atTime(23, 59, 59);

		// 시작일, 종료일 검사
		if (sDateTime.isAfter(eDateTime))
			throw new IllegalArgumentException("종료일이 시작일보다 먼저일 수 없습니다.");

		// 시작일 - 종료일이 90일 이내인지 검사
		if (ChronoUnit.DAYS.between(sDateTime, eDateTime) > 90)
			throw new IllegalArgumentException("검색기간은 90일이내만 가능합니다.");

		// 작성자 없이 검색
		if (Sunpay.isEmpty(writer)) {
			return findByDateAndType(sDateTime, eDateTime, typeCode);
		}
		
		// 타입 없이 검색
		if (Sunpay.isEmpty(typeCode)) {
			return findByDateAndWriter(sDateTime, eDateTime, writer);
		}
		
		return findByDateAndWriterAndType(sDateTime, eDateTime, writer, typeCode);
	}
	
	@Transactional(readOnly=true)
	private List<ContactOtoResponse> findByDateAndWriterAndType(LocalDateTime sDateTime, LocalDateTime eDateTime, String writer, String typeCode) {

		try {
			return contactOtoRepo
					.findByCreatedDateBetweenAndWriterContainingAndTypeCode(sDateTime, eDateTime, writer, typeCode)
					.stream()
					.map(ContactOtoResponse::new)
					.collect(Collectors.toList());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	private List<ContactOtoResponse> findByDateAndWriter(LocalDateTime sDateTime, LocalDateTime eDateTime, String writer) {
		
		// 작성자 없이 검색
		if (Sunpay.isEmpty(writer)) {
			return findByDate(sDateTime, eDateTime);
		}
		
		return contactOtoRepo
				.findByCreatedDateBetweenAndWriterContaining(sDateTime, eDateTime, writer)
				.stream()
				.map(ContactOtoResponse::new)
				.collect(Collectors.toList());
	}
	
	@Transactional(readOnly = true)
	private List<ContactOtoResponse> findByDateAndType(LocalDateTime sDateTime, LocalDateTime eDateTime, String typeCode) {
		
		// 작성자 없이 검색
		if (Sunpay.isEmpty(typeCode)) {
			return findByDate(sDateTime, eDateTime);
		}
		
		return contactOtoRepo
				.findByCreatedDateBetweenAndTypeCode(sDateTime, eDateTime, typeCode)
				.stream()
				.map(ContactOtoResponse::new)
				.collect(Collectors.toList());
	}
	
	@Transactional(readOnly = true) 
	private List<ContactOtoResponse> findByDate(LocalDateTime sDateTime, LocalDateTime eDateTime) {
		
		return contactOtoRepo
				.findByCreatedDateBetween(sDateTime, eDateTime)
				.stream()
				.map(ContactOtoResponse::new)
				.collect(Collectors.toList());
	}
	
	@Transactional
	public void updateAnswer(int memberUid, int uid, ContactOtoAnswerRequest answerRequest) {
		
		// 멤버 검사
		Member answerer = memberService.getMember(memberUid);
		if (answerer == null)
			throw new IllegalArgumentException("멤버 UID를 확인해주세요.");

		// 멤버가 본사 이상급인지 검사
		if (!memberService.hasRole(answerer, MemberService.ROLE_HEAD) && !memberService.hasRole(answerer, MemberService.ROLE_TOP))
			throw new BadCredentialsException("접근권한이 없습니다.");
		
		// 문의글 UID 검사
		ContactOto query = contactOtoRepo.findByUid(uid).orElse(null);
		if (query == null)
			throw new IllegalArgumentException("문의글 UID를 확인해주세요.");
		
		// 이미 답변 완료된 문의글인지 검사
		if (query.getStatusCode().equals(STATUS_FINISH))
			throw new IllegalArgumentException("이미 답변이 완료된 문의글입니다.");
		
		// 답변, 상태 업데이트 
		query.setAnswer(answerRequest.getAnswer());
		query.setAnsweredDate(LocalDateTime.now());
		query.setAnswererUid(memberUid);
		query.setStatusCode(STATUS_FINISH);
		
		contactOtoRepo.save(query);
	}
}
