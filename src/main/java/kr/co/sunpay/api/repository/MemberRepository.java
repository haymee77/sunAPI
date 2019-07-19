package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.co.sunpay.api.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Integer>{
	
	public Optional<Member> findByUid(int uid);
	
	public Optional<Member> findById(String id);
	
	public Optional<Member> findByEmail(String eMail);
	
	public int countByEmail(String eMail);
	
	// API 회원생성 요청으로 인한 쿼리 수정 - API 가입신청 상태인 회원 제외 (2019-06-21:JAEROX)
	@Query(value = "SELECT * FROM SUN.SP_MEMBERS WHERE API_STATUS_CD != '1'", nativeQuery = true)
	public List<Member> findAll();
		
	// API 가입신청 상태인 회원 리스트
	public List<Member> findByApiStatusCd(String apiStatusCd);

}
