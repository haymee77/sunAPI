package kr.co.sunpay.api.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.co.sunpay.api.domain.Config;
import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.model.Fee;
import kr.co.sunpay.api.repository.ConfigRepository;
import kr.co.sunpay.api.repository.GroupRepository;
import kr.co.sunpay.api.repository.MemberRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
	
	public final ConfigRepository configRepo;
	public final Config config;
	
	public GroupService(ConfigRepository configRepo, @Value("${config.sitecode}") String siteCode) {
		this.configRepo = configRepo;
		config = configRepo.findBySiteCode(siteCode).orElse(null);
	}
	
	/**
	 * 본사 그룹을 리턴한다
	 * 
	 * @return
	 */
	public Group findHead() {
		return groupRepo.findByRoleCode(ROLE_HEAD).orElse(null);
	}
	
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
	
	public List<Group> getGroups(Member member) {
		return getGroups(member.getUid());
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
		
		// 대리점 멤버이면 자신의 그룹만 포함
		} else if (memberService.hasRole(member, MemberService.ROLE_AGENCY)) {

			groups.add(member.getGroup());
		
		// 권한 없음..
		} else {
			throw new BadCredentialsException("권한이 없습니다.(Need one of TOP, HEAD, BRANCH, AGENCY qualification.)");
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
		Group group = groupRepo.findByUid(groupUid).orElse(null);
		if (group == null)
			throw new EntityNotFoundException("그룹을 찾을 수 없습니다.");
		
		setParent(group);

		return group;
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
		
		// 그룹 정보 Validation 검사
		Group parent = new Group();
		try {
			parent = getGroup(group.getParentGroupUid());
		} catch (Exception e) {
			throw new IllegalArgumentException("상위 그룹 확인 오류(parentGroupUid 확인)");
		}
		group.setParentGroup(parent);		
		// 소유주 멤버 등록
		List<Member> members = new ArrayList<Member>();
		group.getMembers().forEach(mem -> {
			mem.setPassword(pwEncoder.encode(mem.getPassword()));
			members.add(mem);
		});
		group.setMembers(members);	
		
		// 그룹 기본값 설정 >>
		group.setDeleted(false);
		
		// 그룹 생성 시 PG 수수료는 현재 설정된 값으로 함
		/*
		group.setFeePg(config.getFeePg());
		group.setTransFeePg(config.getTransFeePg());*/
		
		// 본사, 지점, 대리점 속성값 확인 및 기본값 설정
		
		switch (group.getRoleCode()) {
			// 지사
			case ROLE_BRANCH:
				if (!parent.getRoleCode().equals(ROLE_HEAD)) {
					throw new IllegalArgumentException("지사는 본점 하위로만 생성 가능");
				}
				/*
				if (!(group.getFeeHead() >= 0)) {
					throw new IllegalArgumentException("본사 수수료 미입력");
				}
				
				if (!(group.getTransFeeHead() >= 0)) {
					throw new IllegalArgumentException("본사 순간정산 서비스 수수료 미입력");
				}
				
				group.setFeeBranch(0.0);
				*/
				break;
				
			// 대리점
			case ROLE_AGENCY:
				if (!parent.getRoleCode().equals(ROLE_BRANCH)) {
					throw new IllegalArgumentException("대리점은 지사 하위로만 생성 가능");
				}
				/*
				if (!(group.getFeeBranch() >= 0)) {
					throw new IllegalArgumentException("지사 수수료 미입력");
				}
				
				if (!(group.getTransFeeBranch() >= 0)) {
					throw new IllegalArgumentException("지사 송금수수료 미입력");
				}
				
				// 본사 수수료는 지사와 동일하게 셋팅
				group.setFeeHead(parent.getFeeHead());
				group.setTransFeeHead(parent.getTransFeeHead());
				*/
				break;
				
			default:
				throw new IllegalArgumentException("상위 그룹 설정 오류(parentGroupUid 확인)");
				//break;
				
		}

		
		// << 그룹 기본값 설정 끝
		
		Group newGroup = groupRepo.save(group);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uid}").buildAndExpand(newGroup.getUid())
				.toUri();

		return ResponseEntity.created(location).build();
	}

	public void deleteGroup(int uid) {

		Group group = groupRepo.findByUid(uid).orElse(null);
		
		// Cascade 설정때문에 부모-자식관계 제거해주어야 삭제 가능함
		/*
		if (member != null) {
			if (member.getStore() != null) {
				
				member.getStore().getMembers().remove(member);
				
			} else if (member.getGroup() != null) {
				
				member.getGroup().getMembers().remove(member);
			}
		} else {
			throw new IllegalArgumentException("존재하지 않는 멤버UID 입니다.");
		}
		*/

		groupRepo.delete(group);

	}	
	
	
	public ResponseEntity<Object> updateGroup(int memberUid, int groupUid, Group group) {

		// 그룹 수정 권한이 있는 멤버인지 확인
		Member member = memberService.getMember(memberUid);
		if (!memberService.hasGroupQualification(member, group)) {
			throw new BadCredentialsException("해당 그룹에 대한 권한이 없습니다.");
		}

		// 수정할 그룹 정보 가져와서 새로운 정보로 수정
		Group xGroup = getGroup(groupUid);
		Group updatedGroup = editGroup(xGroup, group);
		groupRepo.save(updatedGroup);
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
		
		/*
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
		*/
		
		Group updatedGroup=before;
					
		updatedGroup.setFee(after.getFee());
		updatedGroup.setInstantFee(after.getInstantFee());
		updatedGroup.setTransFee(after.getTransFee());
		
		updatedGroup.setBankCode(after.getBankCode());
		updatedGroup.setBankAccountNo(after.getBankAccountNo());
		updatedGroup.setBankAccountName(after.getBankAccountName());
		
		updatedGroup.setBizTypeCode(after.getBizTypeCode());
		
		updatedGroup.setBizZipcode(after.getBizZipcode());
		updatedGroup.setBizAddressBasic(after.getBizAddressBasic());
		updatedGroup.setBizAddressDetail(after.getBizAddressDetail());
		updatedGroup.setBizContact(after.getBizContact());
		updatedGroup.setBizIndustry(after.getBizIndustry());
		updatedGroup.setBizName(after.getBizName());
		updatedGroup.setBizNo(after.getBizNo());
		updatedGroup.setBizOwner(after.getBizOwner());
		updatedGroup.setBizOwnerRegiNo(after.getBizOwnerRegiNo());
		updatedGroup.setBizStatus(after.getBizStatus());
		
		return updatedGroup;
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
	
	/**
	 * 그룹의 수수료 정보 리턴
	 * * 상위 그룹의 PG 정보 노출을 막기 위해 아래와 같이 함(상위그룹 수수료는 모두 PG수수료로 합산되어보여짐)
	 * Fee.pg: PG수수료 + 상위 그룹 수수료 합산(ex.지사그룹이라면 PG + 본사)
	 * Fee.transPg: PG순간정산(송금) 수수료 + 상위 그룹 수수료 합산(ex.지사그룹이라면 PG + 본사)
	 * Fee.selfPg: 자사(해당그룹)의 수수료
	 * Fee.selfTrans: 자사(해당그룹)의 순간정산 수수료
	 * @param groupUid
	 * @return
	 */
	/*
	public Fee getFee(int groupUid) {
		
		Fee fee = new Fee();
		Group group = getGroup(groupUid);
		
		switch (group.getRoleCode()) {
		case ROLE_HEAD:
			fee.setPg(config.getFeePg());
			fee.setTransPg(config.getTransFeePg());
			break;

		case ROLE_BRANCH:
			fee.setPg(group.getFeePg() + group.getFeeHead());
			fee.setTransPg(group.getTransFeePg() + group.getTransFeeHead());
			
			break;

		case ROLE_AGENCY:
			fee.setPg(group.getFeePg() + group.getFeeHead() + group.getFeeBranch());
			fee.setTransPg(group.getTransFeePg() + group.getTransFeeHead() + group.getTransFeeBranch());
			
			break;
			
		default:
			throw new IllegalArgumentException("그룹 권한정보가 없습니다.");
		}
		
		return fee;
	}
	*/
	
	/**
	 * memberUid가 groupUid에 대한 권한을 가지고 있는 경우 groupUid에 대한 수수료 정보 리턴
	 * (권한 없는 경우 BadCredentialsException 발생)
	 * @param memberUid
	 * @param groupUid
	 * @return
	 */
	/*
	public Fee getFee(int memberUid, int groupUid) {
		
		if (hasAuth(memberUid, groupUid)) {
			return getFee(groupUid);
		} else {
			throw new BadCredentialsException("그룹 수수료 조회 권한이 없습니다.");
		}
	}
	*/
	
	/**
	 * memberUid가 groupUid에 대한 권한을 가진 경우 TRUE 리턴
	 * @param memberUid
	 * @param groupUid
	 * @return
	 */
	public boolean hasAuth(int memberUid, int groupUid) {
		List<Group> authGroups = getGroups(memberUid);
		
		for (Group g : authGroups) {
			if (g.getUid() == groupUid) return true;
		}
		
		return false;
	}
	
	/**
	 * memberUid 권한으로 볼 수 있는 연동대리점 그룹 리스트 반환 (2019-06-19:JAEROX)
	 * 
	 * @param memberUid
	 * @return
	 */
	public List<Group> getAgenciesWithApiAgencyYn(int memberUid, Boolean apiAgencyYn) {

		// 그룹 있는지 확인
		if (groupRepo.count() < 1)
			throw new EntityNotFoundException("등록된 대리점이 없습니다.");

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

			groups = groupRepo.findByRoleCodeAndApiAgencyYn(ROLE_AGENCY, apiAgencyYn);
		// 권한 없음..
		} else {
			throw new BadCredentialsException("권한이 없습니다.(Need one of TOP, HEAD, BRANCH, AGENCY qualification.)");
		}
		
		for (Group g : groups) {
			g = setParent(g);
		}

		return groups;
	}
	
	/**
	 * memberUid 권한으로 볼 수 있는 대리점 그룹 리스트 반환 (2019-06-19:JAEROX)
	 * 
	 * @param memberUid
	 * @return
	 */
	public List<Group> getAgencies(int memberUid) {

		// 그룹 있는지 확인
		if (groupRepo.count() < 1)
			throw new EntityNotFoundException("등록된 대리점이 없습니다.");

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

			groups = groupRepo.findByRoleCodeAndDeleted(ROLE_AGENCY, false);
		// 권한 없음..
		} else {
			throw new BadCredentialsException("권한이 없습니다.(Need one of TOP, HEAD, BRANCH, AGENCY qualification.)");
		}
		
		for (Group g : groups) {
			g = setParent(g);
		}

		return groups;
	}
	
	/**
	 * memberUid 권한으로 볼 수 있는 지사 소속 대리점 그룹 리스트 반환 (2019-06-19:JAEROX)
	 * 
	 * @param memberUid
	 * @return
	 */
	public List<Group> getAgenciesWithParentGroupUid(int memberUid, int parentGroupUid) {

		// 그룹 있는지 확인
		if (groupRepo.count() < 1)
			throw new EntityNotFoundException("등록된 대리점이 없습니다.");

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

			groups = groupRepo.findByRoleCodeAndParentGroupUid(ROLE_AGENCY, parentGroupUid);
		// 권한 없음..
		} else {
			throw new BadCredentialsException("권한이 없습니다.(Need one of TOP, HEAD, BRANCH, AGENCY qualification.)");
		}
		
		for (Group g : groups) {
			g = setParent(g);
		}

		return groups;
	}
	
	/**
	 * memberUid 권한으로 볼 수 있는 지사 소속 연동대리점 그룹 리스트 반환 (2019-06-19:JAEROX)
	 * 
	 * @param memberUid
	 * @return
	 */
	public List<Group> getAgenciesWithParentGroupUidAndApiAgencyYn(int memberUid, int parentGroupUid, Boolean apiAgencyYn) {

		// 그룹 있는지 확인
		if (groupRepo.count() < 1)
			throw new EntityNotFoundException("등록된 대리점이 없습니다.");

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

			groups = groupRepo.findByRoleCodeAndParentGroupUidAndApiAgencyYn(ROLE_AGENCY, parentGroupUid, apiAgencyYn);
		// 권한 없음..
		} else {
			throw new BadCredentialsException("권한이 없습니다.(Need one of TOP, HEAD, BRANCH, AGENCY qualification.)");
		}
		
		for (Group g : groups) {
			g = setParent(g);
		}

		return groups;
	}
	
	/**
	 * memberUid 권한으로 볼 수 있는 지사 그룹 리스트 반환 (2019-06-19:JAEROX)
	 * 
	 * @param memberUid
	 * @return
	 */
	public List<Group> getBranches(int memberUid) {

		// 그룹 있는지 확인
		if (groupRepo.count() < 1)
			throw new EntityNotFoundException("등록된 대리점이 없습니다.");

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

			groups = groupRepo.findByRoleCodeAndApiAgencyYn(ROLE_BRANCH, false);
		// 권한 없음..
		} else {
			throw new BadCredentialsException("권한이 없습니다.(Need one of TOP, HEAD, BRANCH, AGENCY qualification.)");
		}
		
		for (Group g : groups) {
			g = setParent(g);
		}

		return groups;
	}
	
	/**
	 * 대리점 연동 등록  (2019-06-19:JAEROX)
	 * 
	 * @param memberUid
	 * @param groupUid
	 * @param group
	 * @return
	 */
	public Group addApiAgency(int memberUid, int groupUid, Group group) {
		/*
		// 그룹 수정 권한이 있는 멤버인지 확인
		Member member = memberService.getMember(memberUid);
		if (!memberService.hasGroupQualification(member, group)) {
			throw new BadCredentialsException("해당 그룹에 대한 권한이 없습니다.");
		}
		//*/
		
		Group dbGroup = groupRepo.findByUid(groupUid).get();
		dbGroup.setApiAgencyYn(true);
		dbGroup.setApiAgencyCreatedDt(LocalDateTime.now());
		dbGroup.setApiAgencyAdminId(group.getApiAgencyAdminId());
		dbGroup.setApiAgencyBizNm(group.getApiAgencyBizNm());
		if (StringUtils.isEmpty(group.getApiAgencyPushUrl()) == false) dbGroup.setApiAgencyPushUrl(group.getApiAgencyPushUrl());
		dbGroup.setApiAgencyMasterKey(group.getApiAgencyMasterKey());
		dbGroup.setApiAgencyEncryptAlgorithm(group.getApiAgencyEncryptAlgorithm());
		if (StringUtils.isEmpty(group.getApiAgencyRm1()) == false) dbGroup.setApiAgencyRm1(group.getApiAgencyRm1());
		if (StringUtils.isEmpty(group.getApiAgencyRm2()) == false) dbGroup.setApiAgencyRm2(group.getApiAgencyRm2());
		if (StringUtils.isEmpty(group.getApiAgencyRm3()) == false) dbGroup.setApiAgencyRm3(group.getApiAgencyRm3());
		if (StringUtils.isEmpty(group.getApiAgencyRm4()) == false) dbGroup.setApiAgencyRm4(group.getApiAgencyRm4());
		
		// 연동키 생성
		String privateKey = RandomStringUtils.random(32, true, true);
		dbGroup.setApiAgencyPrivateKey(privateKey);

		return groupRepo.save(dbGroup);
	}
	
	/**
	 * 대리점 연동 수정  (2019-07-08:JAEROX)
	 * 
	 * @param memberUid
	 * @param groupUid
	 * @param group
	 * @return
	 */
	public Group editApiAgency(int memberUid, int groupUid, Group group) {
		Group dbGroup = groupRepo.findByUid(groupUid).get();
		dbGroup.setApiAgencyAdminId(group.getApiAgencyAdminId());
		dbGroup.setApiAgencyBizNm(group.getApiAgencyBizNm());
		if (StringUtils.isEmpty(group.getApiAgencyPushUrl()) == false) dbGroup.setApiAgencyPushUrl(group.getApiAgencyPushUrl());
		dbGroup.setApiAgencyMasterKey(group.getApiAgencyMasterKey());
		dbGroup.setApiAgencyEncryptAlgorithm(group.getApiAgencyEncryptAlgorithm());
		if (StringUtils.isEmpty(group.getApiAgencyRm1()) == false) dbGroup.setApiAgencyRm1(group.getApiAgencyRm1());
		if (StringUtils.isEmpty(group.getApiAgencyRm2()) == false) dbGroup.setApiAgencyRm2(group.getApiAgencyRm2());
		if (StringUtils.isEmpty(group.getApiAgencyRm3()) == false) dbGroup.setApiAgencyRm3(group.getApiAgencyRm3());
		if (StringUtils.isEmpty(group.getApiAgencyRm4()) == false) dbGroup.setApiAgencyRm4(group.getApiAgencyRm4());
		
		return groupRepo.save(dbGroup);
	}
	
	/**
	 * 대리점 연동키 수정  (2019-07-08:JAEROX)
	 * 
	 * @param groupUid
	 * @param group
	 * @return
	 */
	public Group editApiAgencyPrivateKey(int groupUid, Group group) {
		Group dbGroup = groupRepo.findByUid(groupUid).get();
		
		// 연동키 재생성
		String privateKey = RandomStringUtils.random(32, true, true);
		dbGroup.setApiAgencyPrivateKey(privateKey);
		
		return groupRepo.save(dbGroup);
	}
}
