package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Code;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.repository.CodeRepository;

@Service
public class CodeService {

	@Autowired
	CodeRepository repo;
	
	@Autowired
	MemberService memberService;
	
	/** 
	 * 모든 공개코드 리스트 리턴
	 * @return
	 */
	public List<Code> getCodes() {
		if (repo.count() < 1) {
			throw new EntityNotFoundException("코드가 존재하지 않습니다.");
		}
		return repo.findByIsPrivate(false);
	}
	
	/**
	 * memberUid 권한으로 볼 수 있는 모든 코드리스트 리턴
	 * @param memberUid
	 * @return
	 */
	public List<Code> getCodes(int memberUid) {
		
		List<Code> codes = new ArrayList<Code>();
		
		codes.addAll(getCodes());
		codes.addAll(getPrivateCodes(memberUid));
		
		return codes;
	}
	
	/**
	 * memberUid 권한으로 볼 수 있는 비공개 코드리스트 리턴
	 * @param memberUid
	 * @return
	 */
	public List<Code> getPrivateCodes(int memberUid) {
		
		List<Code> privateCodes = repo.findByIsPrivate(true);
		List<Code> codes = new ArrayList<Code>();
		
		Member member = memberService.getMember(memberUid);
		if (memberService.hasRole(member, "TOP") || memberService.hasRole(member, "HEAD")) {
			codes = privateCodes;
		} else if (memberService.hasRole(member, MemberService.ROLE_BRANCH)) {
			
			for (Code c : privateCodes) {
				if (c.getAuthorized().indexOf(MemberService.ROLE_BRANCH) > 0) {
					codes.add(c);
				}
			}
		} else if (memberService.hasRole(member, MemberService.ROLE_AGENCY)) {
			
			for (Code c : privateCodes) {
				if (c.getAuthorized().indexOf(MemberService.ROLE_AGENCY) > 0) {
					codes.add(c);
				}
			}
		}
		
		return codes;
	}
	
	/**
	 * 코드 그룹명으로 검색하여 코드(공개) 리스트 리턴
	 * @param groupName
	 * @return
	 */
	public List<Code> getCodes(String groupName) {
		
		if (repo.countByGroupNameAndIsPrivate(groupName, false) < 1) {
			throw new EntityNotFoundException("해당 그룹이 존재하지 않습니다.");
		}
		return repo.findByGroupNameAndIsPrivate(groupName, false);
	}
	
	/**
	 * groupName으로 코드 검색하여 memberUid 권한으로 볼 수 있는 코드만 리턴
	 * @param groupName
	 * @param memberUid
	 * @return
	 */
	public List<Code> getCodes(String groupName, int memberUid) {
		
		List<Code> codes = repo.findByGroupNameAndIsPrivate(groupName, false);
		List<Code> privateCodes = repo.findByGroupNameAndIsPrivate(groupName, true);
		
		Member member = memberService.getMember(memberUid);
		if (memberService.hasRole(member, "TOP") || memberService.hasRole(member, "HEAD")) {
			codes.addAll(privateCodes);
		} else if (memberService.hasRole(member, MemberService.ROLE_BRANCH)) {
			
			for (Code c : privateCodes) {
				if (c.getAuthorized().indexOf(MemberService.ROLE_BRANCH) > 0) {
					codes.add(c);
				}
			}
		} else if (memberService.hasRole(member, MemberService.ROLE_AGENCY)) {
			
			for (Code c : privateCodes) {
				if (c.getAuthorized().indexOf(MemberService.ROLE_AGENCY) > 0) {
					codes.add(c);
				}
			}
		}
		
		return codes;
	}
	
	public List<String> getGroupList() {
		
		List<String> list = repo.findGroupList();
		
		return list;
	}
}
