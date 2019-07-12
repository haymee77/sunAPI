package kr.co.sunpay.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.model.Fee;
import kr.co.sunpay.api.service.GroupService;

@RestController
@RequestMapping("/group")
public class GroupController {

	@Autowired
	GroupService groupService;

	@GetMapping("/{memberUid}")
	@ApiOperation(value = "본사/지사/대리점 리스트 요청(멤버 권한으로 접근 가능한 그룹의 리스트 반환)")
	public List<Group> retrieveGroups(@ApiParam(value = "멤버 고유 번호") @PathVariable(value = "memberUid") int memberUid) {
		
		List<Group> groups = groupService.getGroups(memberUid);
		
		// 수수료 정보 숨김
		for (Group g : groups) {
			g = g.hideFee();
		}
		
		return groups;
	}
	
	@GetMapping("/{memberUid}/{groupUid}")
	@ApiOperation(value = "본사/지사/대리점 그룹 정보")
	public Group getGroup(
			@ApiParam(value = "멤버 고유 번호") @PathVariable(value = "memberUid") int memberUid,
			@ApiParam(value = "그룹 고유 번호") @PathVariable(value = "groupUid") int groupUid) {

		Group group = groupService.getGroup(memberUid, groupUid).hideFee();

		return group;
	}

	@RequestMapping(path="/{memberUid}", method=RequestMethod.POST)
	@ApiOperation(value = "본사/지사/대리점 그룹 등록", notes = "그룹 생성, Header에 location 추가하여 응답함")
	public ResponseEntity<Object> createGroup(
			@ApiParam(value = "멤버 고유 번호") @PathVariable(value = "memberUid") int memberUid, @ApiParam(name="group", value="등록할 그룹 정보") @RequestBody Group group) {
		return groupService.createGroup(memberUid, group);
	}

	@PutMapping("/{memberUid}/{groupUid}")
	@ApiOperation(value = "본사/지사/대리점 그룹 수정", notes = "그룹 수정, Header에 location 추가하여 응답함")
	public ResponseEntity<Object> updateGroup(
			@ApiParam(value = "멤버 고유 번호") @PathVariable(value = "memberUid") int memberUid,
			@ApiParam(value = "그룹 고유 번호") @PathVariable(value = "groupUid") int groupUid, @RequestBody Group group) {

		return groupService.updateGroup(memberUid, groupUid, group);
	}
	
	@DeleteMapping("/{uid}")
	@ApiOperation(value="지사/대리점 삭제 요청", notes="{uid} 지사/대리점 삭제 요청")
	public void deletGroup(@ApiParam("삭제할 지사/대리점의 uid") @PathVariable int uid) {
		
		groupService.deleteGroup(uid);
		
		return;
	}
	/*
	@GetMapping("/fee/{memberUid}/{groupUid}")
	@ApiOperation(value = "그룹 수수료 정보", notes = "")
	public Fee getGroupFee(@ApiParam(value="멤버UID") @PathVariable(value="memberUid") int memberUid, @ApiParam(value = "그룹UID") @PathVariable(value = "groupUid") int groupUid) {

		return groupService.getFee(memberUid, groupUid);
	}
	*/
	
}
