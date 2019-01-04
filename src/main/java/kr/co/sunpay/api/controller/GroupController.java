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
import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.service.GroupService;

@RestController
@RequestMapping("/group")
public class GroupController {

	@Autowired
	GroupService groupService;
	
	@GetMapping("")
	@ApiOperation(value="본사/지사/대리점 그룹 리스트 요청")
	public List<Group> retrieveGroups() {
		return groupService.getGroups();
	}
	
	@PostMapping("")
	@ApiOperation(value="본사/지사/대리점 그룹 등록", notes="그룹 생성, Header에 location 추가하여 응답")
	public ResponseEntity<Object> createGroup(@RequestBody Group group) {
		return groupService.createGroup(group);
	}
}
