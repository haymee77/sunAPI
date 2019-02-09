package kr.co.sunpay.api.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
	PasswordEncoder pwEncoder;

	@Autowired
	MemberRepository memberRepo;

	@Autowired
	MemberService memberService;
	
	public static final String ROLE_HEAD = "HEAD";
	public static final String ROLE_BRANCH = "BRANCH";
	public static final String ROLE_AGENCY = "AGENCY";
	
	/**
	 * 하위 모든 그룹 리스트 반환(leaf 그룹까지 포함)
	 * @param group
	 * @param isRoot
	 * @return
	 */
	public List<Group> getChildren(Group group, boolean isRoot) {
		
		List<Group> groups = new ArrayList<Group>();
	
		// 하위 그룹 가져오기
		List<Group> children = groupRepo.findByparentGroupUid(group.getUid());
		
		if (children != null && children.size() > 0) {
			// 호출 당시 그룹이 아닌경우 자기 자신도 리턴할 그룹리스트에 추가
			if (!isRoot) {
				groups.add(group);
			}
			
			// 하위 그룹 순회하면서 재귀태움 
			for (Group child : children) {
				// 최하위까지 내려갔다가 올라오면서 계속 리스트 더함
				groups.addAll(getChildren(child, false));
			}
		} else {
			// 최하위 그룹까지 온 경우 자기 자신을 리스트에 담아 리턴함
			groups.add(group);
			return groups;
		}
		
		return groups;
	}

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
		Member member = memberService.getMember(memberUid);

		// MANAGER 권한 없으면 권한없음
		if (!memberService.hasRole(member, "MANAGER"))
			throw new BadCredentialsException("권한이 없습니다.(Need MANAGER qualification.)");

		if (member.getGroup() == null)
			throw new BadCredentialsException("권한이 없습니다.(그룹 소속 멤버가 아님)");

		// 멤버권한 확인하여 groups 리스트 생성
		List<Group> groups = new ArrayList<Group>();

		// 최고관리자, 본사 멤버이면 모든 그룹리스트 반환
		if (memberService.hasRole(member, MemberService.ROLE_TOP)
				|| memberService.hasRole(member, MemberService.ROLE_HEAD)) {

			groups = groupRepo.findAll();

		// 지사 멤버이면 해당 지사의 그룹리스트 반환
		} else if (memberService.hasRole(member, MemberService.ROLE_BRANCH)) {

			// 지사 하위 그룹리스
			groups = groupRepo.findByparentGroupUid(member.getGroup().getUid());
			
			// + 멤버 자신의 그룹 포함
			groups.add(member.getGroup());
			

		// 권한 없음..
		} else {
			throw new BadCredentialsException("권한이 없습니다.(Need one of TOP, HEAD, BRANCH qualification.)");
		}
		
		for (Group g : groups) {
			g = setParent(g);
		}

		return groups;
	}
	
	/**
	 * 그룹 엔티티에 상위그룹정보 추가
	 * @param group
	 * @return
	 */
	public Group setParent(Group group) {
		
		if (group.getParentGroupUid() != null && group.getParentGroupUid() > 0) {
			try {
				// 상위업체 정보 가져오기
				Group p = getGroup(group.getParentGroupUid());
				
				// 필요한 정보만 제공
				group.setParentBizName(p.getBizName());
			} catch (EntityNotFoundException ex) {
				throw new EntityNotFoundException("그룹(UID:" + group.getUid() + ")의 상위 업체 정보를 찾을 수 없습니다.");
			}
		}
		
		return group;
	}
	
	public Group getGroup(int groupUid) {
		Optional<Group> group = groupRepo.findByUid(groupUid);
		if (!group.isPresent())
			throw new EntityNotFoundException("그룹을 찾을 수 없습니다.");

		return group.get();
	}
	
	public Group getGroup(int memberUid, int groupUid) {
		
		Member member = memberService.getMember(memberUid);
		Group group = getGroup(groupUid);
		
		if (!memberService.hasGroupQualification(member, group))
			throw new BadCredentialsException("그룹 권한이 없습니다.");
		
		return group;
	}

	public ResponseEntity<Object> createGroup(int memberUid, Group group) {

		// 멤버 정보 확인
		Member member = memberService.getMember(memberUid);

		// MANAGER 권한 없으면 권한없음
		if (!memberService.hasRole(member, "MANAGER"))
			throw new BadCredentialsException("권한이 없습니다.(Need MANAGER qualification.)");

		group.setDeleted(false);
		// 소유주 멤버 등록
		List<Member> members = new ArrayList<Member>();
		group.getMembers().forEach(mem -> {
			mem.setPassword(pwEncoder.encode(mem.getPassword()));
			members.add(mem);
		});
		group.setMembers(members);
		Group newGroup = groupRepo.save(group);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}").buildAndExpand(newGroup.getUid())
				.toUri();

		return ResponseEntity.created(location).build();
	}

	public ResponseEntity<Object> updateGroup(int memberUid, int groupUid, Group group) {

		// 그룹 수정 권한이 있는 멤버인지 확인
		Member member = memberService.getMember(memberUid);
		if (!memberService.hasGroupQualification(member, group)) {
			throw new BadCredentialsException("해당 그룹에 대한 권한이 없습니다.");
		}

		// 수정할 그룹 정보 가져와서 새로운 정보로 수정
		Group xGroup = getGroup(groupUid);
		group = editGroup(xGroup, group);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();

		return ResponseEntity.created(location).build();
	}

	/**
	 * 수정 가능한 항목만 수정함
	 * 
	 * @param before
	 * @param after
	 * @return
	 */
	public Group editGroup(Group before, Group after) {

		if (after.getBankAccountName() != null && !after.getBankAccountName().isEmpty()) {
			before.setBankAccountName(after.getBankAccountName());
		}

		if (after.getBankAccountNo() != null && !after.getBankAccountNo().isEmpty()) {
			before.setBankAccountNo(after.getBankAccountNo());
		}

		if (after.getBankCode() != null && !after.getBankCode().isEmpty()) {
			before.setBankCode(after.getBankCode());
		}

		if (after.getOwnerMemberUid() > 0) {
			// 기존 소유주멤버의 OWNER 권한 제거 및 새소유주멤버에 OWNER권한 추가
			changeOwnerTo(before, after.getOwnerMemberUid());
			before.setOwnerMemberUid(after.getOwnerMemberUid());
		}

		return before;
	}

	/**
	 * 그룹의 소유주(OWNER)권한을 갖는 멤버를 전달받은 memberUid로 변경함 해당 그룹의 멤버여야만 소유주가 될 수 있음 소유주는
	 * 1명이어야하므로 이전 소유주는 OWNER권한 박탈
	 * 
	 * @param group
	 * @param memberUid
	 */
	public void changeOwnerTo(Group group, int memberUid) {

		Member member = memberService.getMember(memberUid);

		if (!group.getMembers().contains(member)) {
			throw new BadCredentialsException("해당 그룹 소속 멤버만이 소유주가 될 수 있습니다.");
		}

		if (group.getMembers() != null && !group.getMembers().isEmpty()) {
			// 기존 멤버 소유주의 OWNER 권한 박탈
			for (Member mem : group.getMembers()) {
				if (memberService.hasRole(mem, MemberService.ROLE_OWNER) && mem.getUid() != memberUid) {
					memberService.removeRole(mem, MemberService.ROLE_OWNER);
					memberRepo.save(mem);
				}
			}
		}

		memberService.addRole(memberUid, MemberService.ROLE_OWNER);
	}
}
