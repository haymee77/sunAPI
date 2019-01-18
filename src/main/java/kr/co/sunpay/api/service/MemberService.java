package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.MemberRole;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.repository.GroupRepository;
import kr.co.sunpay.api.repository.MemberRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
@Service
public class MemberService {

	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	GroupRepository groupRepo;

	@Autowired
	PasswordEncoder pwEncoder;
	
	@Autowired
	StoreService storeService;
	
	private final String ROLE_TOP = "TOP";
	private final String ROLE_HEAD = "HEAD";
	private final String ROLE_BRANCH = "BRANCH";
	private final String ROLE_AGENCY = "AGENCY";
	private final String ROLE_MANAGER = "MANAGER";
	private final String ROLE_STAFF = "STAFF";
	private final String ROLE_CS = "CS";
	private final String ROLE_OWNER = "OWNER";

	private Member member;
	
	public Member getMember(int uid) {
		
		Member member;
		Optional<Member> getMember = memberRepo.findByUid(uid);
		
		if (!getMember.isPresent())
			throw new EntityNotFoundException("There is no Member(uid:" + uid + ")");
		
		member = getMember.get();
		
		if (member.getStore() != null) {
			member.setStoreName(member.getStore().getBizName());
		}
		
		if (member.getGroup() != null) {
			member.setGroupName(member.getGroup().getBizName());
		}
		
		return member;
	}
	
	/**
	 * memberUid 멤버의 권한으로 확인 가능한 멤버리스트 반환
	 * @param memberUid
	 * @return
	 */
	public List<Member> getMembers(int memberUid) {
		Member member = getMember(memberUid);
		List<Member> members = new ArrayList<Member>();
		
		if (hasRole(member, ROLE_TOP) || hasRole(member, ROLE_HEAD)) {
			
			members = getMembers();
			
		} else if (hasRole(member, ROLE_BRANCH) || hasRole(member, ROLE_AGENCY)) {
			
			members = getMembers(member.getGroup());
			
		}

		return members;
	}
	
	/**
	 * group의 멤버리스트 반환
	 * @param group
	 * @return
	 */
	public List<Member> getMembers(Group group) {
		
		List<Member> members = new ArrayList<Member>();
		
		if (group.getRoleCode().equals("AGENCY")) {
			
			members = group.getMembers();
			
			for (Store store : group.getStores()) {
				members.addAll(store.getMembers());
			}
			
		} else if (group.getRoleCode().equals("BRANCH")) {
			
			members = group.getMembers();
			
			List<Group> agencies = groupRepo.findByparentGroupUid(group.getParentGroupUid());
			for (Group agency : agencies) {
				members.addAll(getMembers(agency));
			}
		}
		
		return members;
	}
	
	public List<Member> getMembers() {
		
		if (memberRepo.count() == 0)
			throw new EntityNotFoundException("There is no availabel Member");
		
		List<Member> members = new ArrayList<Member>();
		memberRepo.findAll().forEach(member -> {
			
			if (member.getStore() != null) {
				member.setStoreName(member.getStore().getBizName());
			}
			
			if (member.getGroup() != null) {
				member.setGroupName(member.getGroup().getBizName());
			}
			
			members.add(member);
		});
		
		return members;
	}

	public Member createMember(Member member) {

		log.info("-- MemberService.createMember called..");
		if (!memberRepo.findById(member.getId()).isEmpty()) {
			throw new DuplicateKeyException("아이디 중복");
		}

		Member newMem = new Member();
		newMem.setActivate(true);
		newMem.setEmail(member.getEmail());
		newMem.setId(member.getId());
		newMem.setName(member.getName());
		newMem.setMobile(member.getMobile());
		newMem.setPassword(pwEncoder.encode(member.getPassword()));

		List<MemberRole> roles = new ArrayList<MemberRole>();
		member.getRoles().forEach(role -> {
			roles.add(role);
		});

		newMem.setRoles(roles);

		return memberRepo.save(newMem);
	}

	public void deleteMember(int uid) {

		checkUid(uid);
		memberRepo.delete(memberRepo.findByUid(uid).get());
	}

	public Member updateMember(int uid, Member member) {

		checkUid(uid);

		Member dbMember = memberRepo.findByUid(uid).get();

		if (member.getPassword() != null && member.getPassword().trim().length() > 0) {
			dbMember.setPassword(pwEncoder.encode(member.getPassword()));
		}

		dbMember.setActivate(member.getActivate());
		dbMember.setEmail(member.getEmail());
		dbMember.setMobile(member.getMobile());
		dbMember.setName(member.getName());

		// 새로운 권한 추가
		member.getRoles().forEach(role -> {
			if (!dbMember.getRoles().contains(role)) {
				dbMember.getRoles().add(role);
			}
		});

		// 제거된 권한 삭제
		Iterator<MemberRole> iRoles = dbMember.getRoles().iterator();
		while (iRoles.hasNext()) {
			MemberRole role = iRoles.next();

			if (!member.getRoles().contains(role)) {
				iRoles.remove();
			}
		}

		// 상점ID, 그룹ID 수정
		if (member.getStore() != null) {
			dbMember.setStore(member.getStore());
		}

		if (member.getGroup() != null) {
			dbMember.setGroup(member.getGroup());
		}

		return memberRepo.save(dbMember);
	}
	
	public void addRole(int memberUid, String roleName) {
		Member member = getMember(memberUid);
		addRole(member, roleName);
	}
	
	public void addRole(Member member, String roleName) {
		
		if (!hasRole(member, roleName)) {
			MemberRole newRole = new MemberRole();
			newRole.setRoleName(roleName);
			member.getRoles().add(newRole);
		}
		
		memberRepo.save(member);
	}
	
	public void removeRole(int memberUid, String roleName) {
		Member member = getMember(memberUid);
		removeRole(member, roleName);
	}
	
	public void removeRole(Member member, String roleName) {
		
		if (hasRole(member, roleName)) {
			Iterator<MemberRole> roles = member.getRoles().iterator();
			while (roles.hasNext()) {
				MemberRole r = roles.next();
				if (r.getRoleName().equals(roleName)) roles.remove();
			}
			
			memberRepo.save(member);
		}
	}

	public void checkUid(int uid) {

		if (!memberRepo.findByUid(uid).isPresent()) {
			throw new EntityNotFoundException("There is No Member[uid=" + uid + "]");
		}
	}

	public List<String> getRoleNames(Member member) {

		List<String> roleNames = new ArrayList<String>();

		member.getRoles().forEach(role -> {
			roleNames.add(role.getRoleName());
		});

		return roleNames;
	}
	
	public boolean hasRole(Member member, String roleName) {
		
		return getRoleNames(member).contains(roleName);
	}

	public boolean hasStoreQualification(Member member, Store store) {

		boolean qualified = false;
		List<String> memberRoleNames = getRoleNames(member);

		// 최고관리자, 본사 멤버의 경우 자격있음
		if (memberRoleNames.contains(ROLE_TOP) || memberRoleNames.contains(ROLE_HEAD)) {
			System.out.println("TOP/HEAD 권한 - 자격있음");
			qualified = true;

			// 멤버와 상점이 같은 그룹의 소속인 경우 - 자격있음
		} else if (member.getGroup().equals(store.getGroup())) {

			System.out.println("같은 그룹 소속 - 자격있음");
			qualified = true;

		} else {

			// 멤버 소속 그룹에 포함된 모든 상점 리스트 가져옴
			List<Store> stores = storeService.getStores(member.getGroup().getUid());

			// 현재 상점이 멤버 소속 그룹 하위에 있는지 확인
			for (Store s : stores) {
				if (s.getUid() == store.getUid()) {
					System.out.println("소속상점에 포함됨 - 자격있음");
					qualified = true;
					break;
				}
			}
		}

		return qualified;
	}
	
	/**
	 * 그룹에 대한 수정권한 확인
	 * 1. 관리자 권한이 없다면 REJECT
	 * 2. 최고관리자 || 본사멤버라면 OK
	 * 3. 지사그룹이고 해당지사 멤버라면 OK
	 * 4. 대리점그룹이고 해당그룹 멤버라면 OK
	 * 5. 대리점그룹이고 지사멤버이며 대리점의 상위그룹이 멤버의 지사라면 OK
	 * @param member
	 * @param group
	 * @return
	 */
	public boolean hasGroupQualification(Member member, Group group) {
		
		System.out.println(member.getGroup().getUid());
		System.out.println(group.getRoleCode());
		System.out.println(group.getUid());
		
		// 1. 관리자 권한이 없다면 REJECT
		if (!hasRole(member, ROLE_MANAGER))
			return false;
		
		// 2. 최고관리자 || 본사멤버라면 OK
		if (hasRole(member, ROLE_TOP) || hasRole(member, ROLE_HEAD))
			return true;
		
		// 본사그룹은 1개라고 가정하여 본사그룹에 대한 체크는 생략함
		
		// 3. 지사그룹이고 해당지사 멤버라면 OK
		if (group.getRoleCode().equals("BRANCH") && member.getGroup().getUid() == group.getUid())
			return true;
		
		// 4. 대리점그룹이고 해당그룹 멤버라면 OK
		if (group.getRoleCode().equals("AGENCY") && member.getGroup().getUid() == group.getUid())
			return true;
		
		// 5. 대리점그룹이고 지사멤버이며 대리점의 상위그룹이 멤버의 지사라면 OK
		if (group.getRoleCode().equals("AGENCY") && member.getGroup().getUid() == group.getParentGroupUid())
			return true;
		
		return false;
	}
}
