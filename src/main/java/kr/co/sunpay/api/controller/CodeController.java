package kr.co.sunpay.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Code;
import kr.co.sunpay.api.service.CodeService;

@RestController
@RequestMapping("/code")
public class CodeController {
	
	@Autowired
	CodeService codeService;
	
	@GetMapping("")
	@ApiOperation(value="모든 공개 코드 리스트 요청")
	public List<Code> retrieveCodes() {
		return codeService.getCodes();
	}
	
	@GetMapping("/private/{memberUid}")
	@ApiOperation(value="memberUid 로 접근 가능한(비공개 포함) 모든 코드 리스트 요청")
	public List<Code> retrievePrivateCodes(@ApiParam("멤버UID") @PathVariable int memberUid) {
		return codeService.getCodes(memberUid);
	}

	@GetMapping("/{groupName}")
	@ApiOperation(value="특정 그룹의 코드 리스트 요청")
	public List<Code> codesInGroup(@ApiParam("코드 그룹명") @PathVariable String groupName) {
		return codeService.getCodes(groupName);
	}
	
	@GetMapping("/private/{groupName}/{memberUid}")
	@ApiOperation(value="memberUid 로 접근 가능한(비공개 포함) 특정 그룹의 코드 리스트 요청")
	public List<Code> privateCodesInGroup(@ApiParam("코드 그룹명") @PathVariable String groupName,
			@ApiParam("멤버UID") @PathVariable int memberUid) {
		return codeService.getCodes(groupName, memberUid);
	}
	
	@GetMapping("/groups")
	@ApiOperation(value="사용 가능한 코드 그룹 리스트", notes="문자열 리스트로 반환")
	public List<String> getGroupList() {
		return codeService.getGroupList();
	}
}
