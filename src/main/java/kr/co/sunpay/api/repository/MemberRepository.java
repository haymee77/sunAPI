package kr.co.sunpay.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Integer>{
	
	public Optional<Member> findByUid(int uid);
	
	public Optional<Member> findById(String id);

}
