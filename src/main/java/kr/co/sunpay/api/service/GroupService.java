package kr.co.sunpay.api.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.repository.GroupRepository;
import kr.co.sunpay.api.repository.MemberRepository;

@Service
public class GroupService {

	@Autowired
	GroupRepository groupRepo;

	@Autowired
	MemberRepository memberRepo;

	@Autowired
	MemberService memberService;

	/**
	 * memberUid 권한으로 볼 수 있는 모든 그룹 리스트 반환
	 * 
	 * @param memberUid
	 * @return
	 */
	public List<Group> getGroups(int memberUid) {

		// 그룹 있는지 확인
		if (groupRepo.count() < 1)
			throw new EntityNotFoundException("등록된 본사/지사/대리점이 없습니다.");

		// 멤버 정보 확인
		memberService.checkUid(memberUid);
		Member member = memberRepo.findByUid(memberUid).get();

		// MANAGER 권한 없으면 권한없음
		if (!memberService.hasRole(member, "MANAGER"))
			throw new BadCredentialsException("권한이 없습니다.(Need MANAGER qualification.)");
		
		if (member.getGroup() == null)
			throw new BadCredentialsException("권한이 없습니다.(그룹 소속 멤버가 아님)");
		
		// 멤버권한 확인하여 groups 리스트 생성
		List<Group> groups = new ArrayList<Group>();
		
		// 최고관리자, 본사 멤버이면 모든 그룹리스트 반환
		if (memberService.hasRole(member, memberService.getROLE_TOP()) || memberService.hasRole(member, memberService.getROLE_HEAD())) {
			
			groups = groupRepo.findAll();
			
		// 지사 멤버이면 해당 지사의 그룹리스트 반환
		} else if (memberService.hasRole(member, memberService.getROLE_BRANCH())) {
			
			groups = groupRepo.findByparentGroupUid(member.getGroup().getUid());
			
		// 권한 없음..
		} else {
			throw new BadCredentialsException("권한이 없습니다.(Need one of TOP, HEAD, BRANCH qualification.)");
		}
		
		return groups;
	}

	public ResponseEntity<Object> createGroup(Group group) {

		group.setDeleted(false);
		Group newGroup = groupRepo.save(group);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}").buildAndExpand(newGroup.getUid())
				.toUri();

		return ResponseEntity.created(location).build();
	}
}
