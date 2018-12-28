package kr.co.sunpay.api.controller;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.repository.MemberRepository;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/member")
public class MemberController {

	@Autowired
	MemberRepository memberRepo;
	
	@GetMapping("/{uid}")
	@ApiOperation(value="특정 멤버 정보 요청", notes="{uid} 멤버에 대한 정보 반환")
	public Member retrieveMember(@ApiParam("정보를 얻을 멤버의 UID") @PathVariable int uid) {
		
		Optional<Member> getMember = memberRepo.findByUid(uid);
		if (!getMember.isPresent())
			throw new EntityNotFoundException("There is no Member(uid:" + uid + ")");
		
		return getMember.get();
	}
	
	@GetMapping("")
	@ApiOperation(value="멤버 리스트 요청", notes="멤버 리스트 반환")
	public List<Member> retrieveMembers() {
		
		if (memberRepo.count() == 0)
			throw new EntityNotFoundException("There is no availabel Member");
		
		return memberRepo.findAll();
	}
	
}
