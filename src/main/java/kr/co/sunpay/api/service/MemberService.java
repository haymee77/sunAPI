package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.MemberRole;
import kr.co.sunpay.api.repository.MemberRepository;
import lombok.extern.java.Log;

@Log
@Service
public class MemberService {

	@Autowired
	MemberRepository memberRepo;

	@Autowired
	PasswordEncoder pwEncoder;

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
}
