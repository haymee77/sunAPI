package kr.co.sunpay.api.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.repository.MemberRepository;
import kr.co.sunpay.api.service.MemberService;
import lombok.extern.java.Log;

@Log
@RestController
@RequestMapping("/member")
public class MemberController {

	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	PasswordEncoder pwEncoder;
	
	@Autowired
	MemberService memberService;
	
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
	
	@PostMapping("")
	@ApiOperation(value="멤버 생성 요청", notes="멤버 생성 후 URL 반환")
	public ResponseEntity<Object> createMember(@RequestBody Member member) {
		
		log.info("-- MemberController.createMember called...");
		Member newMember = memberService.createMember(member);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}")
						.buildAndExpand(newMember.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@DeleteMapping("/{uid}")
	@ApiOperation(value="멤버 삭제 요청", notes="{uid} 멤버 삭제")
	public void deleteMember(@ApiParam("삭제할 멤버의 uid") @PathVariable int uid) {
		
		memberService.deleteMember(uid);
		
		return;
	}
	
	@PutMapping("/{uid}")
	@ApiOperation(value="멤버 수정 요청", notes="{uid} 멤버 수정")
	public ResponseEntity<Object> updateMember(@ApiParam("수정할 멤버의 uid") @PathVariable int uid,
			@RequestBody Member member) {
		
		Member updatedMember = memberService.updateMember(uid, member);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}")
						.buildAndExpand(updatedMember.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
}
