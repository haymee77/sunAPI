package kr.co.sunpay.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.service.GroupService;

@RestController
@RequestMapping("/group")
public class GroupController {

	@Autowired
	GroupService groupService;
	
	@GetMapping("/{memberUid}")
	@ApiOperation(value="본사/지사/대리점 리스트 요청(멤버 권한으로 접근 가능한 그룹의 리스트 반환)")
	public List<Group> retrieveGroups(@ApiParam(value="멤버 고유 번호") @PathVariable(value="memberUid") int memberUid) {
		return groupService.getGroups(memberUid);
	}
	
	@PostMapping("")
	@ApiOperation(value="본사/지사/대리점 그룹 등록", notes="그룹 생성, Header에 location 추가하여 응답")
	public ResponseEntity<Object> createGroup(@RequestBody Group group) {
		return groupService.createGroup(group);
	}
}
