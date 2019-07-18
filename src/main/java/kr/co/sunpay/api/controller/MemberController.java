package kr.co.sunpay.api.controller;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.apache.ibatis.javassist.NotFoundException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.model.MemberRequest;
import kr.co.sunpay.api.model.MemberResponse;
import kr.co.sunpay.api.repository.MemberRepository;
import kr.co.sunpay.api.service.MemberService;
import kr.co.sunpay.api.util.Sunpay;

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
	public MemberResponse retrieveMember(@ApiParam("정보를 얻을 멤버의 UID") @PathVariable int uid) {
		
		return memberService.getMemberResponse(uid);
	}
	
	@GetMapping("/list/{memberUid}")
	@ApiOperation(value="멤버 리스트 요청", notes="멤버 권한으로 볼 수 있는 멤버 리스트 반환")
	public List<MemberResponse> retrieveMembers(@ApiParam("요청자 UID") @PathVariable int memberUid) {
		// 에러 발생시, 상세 에러가 나오지 않아서 "e.printStackTrace"사용을 위해 try catch 문 사용
		try {
			return memberService.getMembers(memberUid);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@PostMapping("")
	@ApiOperation(value="멤버 생성 요청", notes="멤버 생성 후 GET URL 반환")
	public ResponseEntity<Object> regist(@RequestBody @Valid MemberRequest member) {
		
		Member newMember = memberService.regist(member);
		
		if (Sunpay.isEmpty(newMember)) {
			throw new IllegalArgumentException("Cannot regist inquery");
		}
		
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
	@ApiOperation(value="멤버 수정 요청", notes="{uid} 멤버 수정 소속권한, 기타권한에 대한 수정내용은 적용되지 않음")
	public ResponseEntity<Object> updateMember(@ApiParam("수정할 멤버의 uid") @PathVariable int uid,
			@RequestBody Member member) {
		
		Member updatedMember = memberService.updateMember(uid, member);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}")
						.buildAndExpand(updatedMember.getUid()).toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@GetMapping("/check/id")
	@ApiOperation(value="ID 유무검사", notes="")
	public ResponseEntity<Object> checkId(@RequestParam("checkId") String checkId) throws NotFoundException {
		
		if (memberService.hasMember(checkId)) {
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@GetMapping("/check/mail")
	@ApiOperation(value="Mail 유무검사", notes="")
	public ResponseEntity<Object> checkMail(@RequestParam("checkMail") String checkMail) throws NotFoundException {
		
		if (memberService.countMail(checkMail) > 0) {
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
