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

import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.MemberRole;
import kr.co.sunpay.api.domain.Store;
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
	
	public boolean hasRole(Member member, String role) {
		
		return getRoleNames(member).contains(role);
	}

	public boolean hasStoreQualification(Member member, Store store) {

		boolean qualified = false;
		List<String> memberRoleNames = getRoleNames(member);

		// 최고관리자, 본사 멤버의 경우 자격있음
		if (memberRoleNames.contains("TOP") || memberRoleNames.contains("HEAD")) {
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
}
