package kr.co.sunpay.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.MemberRole;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.model.MemberRequest;
import kr.co.sunpay.api.model.MemberResponse;
import kr.co.sunpay.api.repository.GroupRepository;
import kr.co.sunpay.api.repository.MemberRepository;
import kr.co.sunpay.api.util.Sunpay;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
@Service
public class MemberService extends Sunpay {

	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	GroupRepository groupRepo;

	@Autowired
	PasswordEncoder pwEncoder;
	
	@Autowired
	StoreService storeService;
	
	@Autowired
	GroupService groupService;
	
	// 소속 권한
	public static final String ROLE_TOP = "TOP";
	public static final String ROLE_HEAD = "HEAD";
	public static final String ROLE_BRANCH = "BRANCH";
	public static final String ROLE_AGENCY = "AGENCY";
	public static final String ROLE_STORE = "STORE";
	
	private final static String BELONG_TO_STORE = "STORE";
	private final static String BELONG_TO_GROUP = "GROUP";
	
	public static final String ROLE_MANAGER = "MANAGER";
	public static final String ROLE_STAFF = "STAFF";
	public static final String ROLE_CS = "CS";
	public static final String ROLE_OWNER = "OWNER";
	public static final String ROLE_DEV = "DEV";
	
	// 멤버 수정 시 수정할 수 없는 권한
	public static final List<String> UNAMENDABLE_ROLES = Arrays.asList(
		ROLE_HEAD, ROLE_BRANCH, ROLE_AGENCY, ROLE_STORE, ROLE_OWNER, ROLE_DEV
	);

	private Member member;
	
	/**
	 * 멤버UID로 멤버 정보 리턴
	 * @param uid
	 * @return
	 */
	public Member getMember(int uid) {
		
		return memberRepo.findByUid(uid).orElse(null);
	}
	
	/**
	 * MemberResponse 객체로 회원정보 리턴
	 * @param uid
	 * @return
	 */
	public MemberResponse getMemberResponse(int uid) {
		
		return memberRepo.findByUid(uid).map(MemberResponse::new).orElse(null);
	}
	
	public int countMail(String mail) {
		
		return memberRepo.countByEmail(mail);
	}
	
	/**
	 * memberUid 멤버의 권한으로 확인 가능한 멤버리스트 반환
	 * @param memberUid
	 * @return
	 */
	public List<MemberResponse> getMembers(int memberUid) {
		
		Member member = getMember(memberUid);
		List<MemberResponse> members = new ArrayList<MemberResponse>();
		
		// 본사권한인 경우 모든 멤버리스트 반환
		if (hasRole(member, ROLE_TOP) || hasRole(member, ROLE_HEAD)) {
			
			members = getMembers();
		
		// 지사, 대리점 권한인 경우 해당 그룹 하위의 멤버만 반환
		} else if (hasRole(member, ROLE_BRANCH) || hasRole(member, ROLE_AGENCY)) {
			
			members = getMembers(member.getGroup());
			
		}

		return members;
	}
	
	/**
	 * group 소속의 멤버리스트 반환(하위 포함)
	 * @param group
	 * @return
	 */
	public List<MemberResponse> getMembers(Group group) {
		
		List<MemberResponse> members = new ArrayList<MemberResponse>();
		
		// 대리점인 경우 - 해당 대리점과 대리점 하위 상점의 멤버리스트 반환
		if (group.getRoleCode().equals("AGENCY")) {
			
			members = group.getMembers().stream().map(MemberResponse::new).collect(Collectors.toList());
			
			for (Store store : group.getStores()) {
				members.addAll(store.getMembers().stream().map(MemberResponse::new).collect(Collectors.toList()));
			}

		// 지사의 경우 - 해당 지사와 하위 대리점, 상점의 멤버리스트 반환
		} else if (group.getRoleCode().equals("BRANCH")) {
			
			// 지사의 회원 리스트
			members = group.getMembers().stream().map(MemberResponse::new).collect(Collectors.toList());
			
			// 지사 소속 상점의 회원리스트
			for (Store store : group.getStores()) {
				members.addAll(store.getMembers().stream().map(MemberResponse::new).collect(Collectors.toList()));
			}
			
			// 지사 소속 대리점의 회원리스트
			List<Group> agencies = groupRepo.findByparentGroupUid(group.getUid());
			for (Group agency : agencies) {
				members.addAll(getMembers(agency));
			}
		}
		
		return members;
	}
	
	public List<MemberResponse> getMembers() {
		
		if (memberRepo.count() == 0)
			throw new EntityNotFoundException("There is no availabel Member");
		
		return memberRepo.findAll().stream().map(MemberResponse::new).collect(Collectors.toList());
	}
	
	public boolean hasMember(String id) {
		log.info("-- MemberService.hasMember called..");
		if (!memberRepo.findById(id).isPresent()) {
			return false;
		}
		
		return true;
	}

	/**
	 * 멤버 생성
	 * @param member
	 * @return
	 */
	public Member regist(MemberRequest member) {
	
		// 아이디 중복 검사
		if (hasMember(member.getId())) {
			throw new DuplicateKeyException("아이디 중복");
		}
		
		// 이메일 중복 검사
		if (!Sunpay.isEmpty(member.getEmail()) && countMail(member.getEmail()) > 0) {
			throw new DuplicateKeyException("이메일 중복");
		}
		
		// 비밀번호 암호화
		member.setPassword(pwEncoder.encode(member.getPassword()));
		
		// 상점 소속 회원
		if (BELONG_TO_STORE.equals(member.getBelongTo())) {
			
			// 상점 권한 체크
			if (!hasRole(member.getRoles(), ROLE_STORE)) {
				throw new IllegalArgumentException("필수 권한 없음(상점회원 - 상점권한 필요)");
			}
			
			// 상점 검색
			if (Sunpay.isEmpty(member.getStoreUid())) {
				throw new IllegalArgumentException("소속 상점 미기재");
			}
			
			Store store = storeService.getStore(member.getStoreUid());
			
			if (Sunpay.isEmpty(store)) {
				throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다.");
			}
			
			// 상점회원으로 등록
			return memberRepo.save(member.toEntity(store));
			
		// 그룹 소속 회원	
		} else if (BELONG_TO_GROUP.equals(member.getBelongTo())) {
			
			// 그룹 권한 체크
			if (!hasRole(member.getRoles(), ROLE_HEAD) && !hasRole(member.getRoles(), ROLE_BRANCH) && !hasRole(member.getRoles(), ROLE_AGENCY)) {
				throw new IllegalArgumentException("필수 권한 없음(그룹회원 - 본사, 지사, 대리점 권한 중 한가지 필요)");
			}
			
			// 그룹 검색
			if (Sunpay.isEmpty(member.getGroupUid())) {
				throw new IllegalArgumentException("소속 그룹 미기재");
			}
			
			Group group = groupService.getGroup(member.getGroupUid());
			
			if (Sunpay.isEmpty(group)) {
				throw new IllegalArgumentException("그룹 정보를 찾을 수 없습니다.");
			}
			
			// 그룹회원으로 등록
			return memberRepo.save(member.toEntity(group));
			
		} else {
			throw new IllegalArgumentException("가입 정보 오류");
		}
	}
	
	public Member createMember(Member member) {

		log.info("-- MemberService.createMember called..");
		if (hasMember(member.getId())) {
			throw new DuplicateKeyException("아이디 중복");
		}
		
		if (!Sunpay.isEmpty(member.getEmail()) && countMail(member.getEmail()) > 0) {
			throw new DuplicateKeyException("이메일 중복");
		}
		
		if (Sunpay.isEmpty(member.getAgreeEventMail())) {
			throw new IllegalArgumentException("이벤트 메일 수신 동의 여부 미체크");
		}

		// 멤버 생성
		Member newMem = new Member();
		newMem.setActivate(true);
		newMem.setEmail(member.getEmail());
		newMem.setId(member.getId());
		newMem.setName(member.getName());
		newMem.setMobile(member.getMobile());
		newMem.setPassword(pwEncoder.encode(member.getPassword()));
		newMem.setAgreeEventMail(member.getAgreeEventMail());
		
		if (member.getGroup() == null && member.getStore() == null) {
			throw new IllegalArgumentException("그룹, 상점 중 한가지 필수입력");
		} else if (member.getGroup() != null && member.getStore() != null) {
			throw new IllegalArgumentException("그룹, 상점 중 한가지만 입력");
		}
		
		newMem.setStore(member.getStore());
		newMem.setGroup(member.getGroup());
		
		List<MemberRole> roles = new ArrayList<MemberRole>();
		member.getRoles().forEach(role -> {
			roles.add(role);
		});

		newMem.setRoles(roles);

		return memberRepo.save(newMem);
	}

	public void deleteMember(int uid) {

		Member member = memberRepo.findByUid(uid).orElse(null);
		
		// Cascade 설정때문에 부모-자식관계 제거해주어야 삭제 가능함
		if (member != null) {
			if (member.getStore() != null) {
				
				member.getStore().getMembers().remove(member);
				
			} else if (member.getGroup() != null) {
				
				member.getGroup().getMembers().remove(member);
			}
		} else {
			throw new IllegalArgumentException("존재하지 않는 멤버UID 입니다.");
		}

		memberRepo.delete(member);

	}

	public Member updateMember(int uid, Member member) {

		checkUid(uid);

		Member dbMember = memberRepo.findByUid(uid).get();

		if (member.getPassword() != null && member.getPassword().trim().length() > 0) {
			dbMember.setPassword(pwEncoder.encode(member.getPassword()));
		}
		
		if (Sunpay.isEmpty(member.getAgreeEventMail())) {
			throw new IllegalArgumentException("이벤트 메일 수신 동의 여부 미체크");
		}

		dbMember.setActivate(member.getActivate());
		dbMember.setEmail(member.getEmail());
		dbMember.setMobile(member.getMobile());
		dbMember.setName(member.getName());
		dbMember.setAgreeEventMail(member.getAgreeEventMail());
		
		log.info("-- 멤버 수정");
		log.info(dbMember.getAgreeEventMail() + "");

		// 권한 변경 적용
		// 새로운 권한 추가
		if (Sunpay.isEmpty(member.getRoles()) && member.getRoles().size() > 0) {
			for (MemberRole role : member.getRoles()) {
				if (UNAMENDABLE_ROLES.contains(role.getRoleName())) continue;
				if (!dbMember.getRoles().contains(role)) {
					dbMember.getRoles().add(role);
				}
			}
			
			// 제거된 권한 삭제
			Iterator<MemberRole> iRoles = dbMember.getRoles().iterator();
			while (iRoles.hasNext()) {
				MemberRole role = iRoles.next();

				if (UNAMENDABLE_ROLES.contains(role.getRoleName())) continue;
				if (!member.getRoles().contains(role)) {
					iRoles.remove();
				}
			}
		}
		
		// 상점ID, 그룹ID 수정 불가 - 소속 변경은 불가능함. 
		// TODO 소속변경에 대한 요청 시 다방면으로 고려해볼 것.

		return memberRepo.save(dbMember);
	}
	
	public void addRole(int memberUid, String roleName) {
		Member member = getMember(memberUid);
		addRole(member, roleName);
	}
	
	public void addRole(Member member, String roleName) {
		
		if (!hasRole(member, roleName)) {
			member.getRoles().add(new MemberRole(roleName));
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
	
	/**
	 * 멤버가 특정 권한을 가졌는지 체크
	 * @param member
	 * @param roleName
	 * @return
	 */
	public boolean hasRole(Member member, String roleName) {
		
		return hasRole(member.getRoles(), roleName);
	}
	
	/**
	 * 권한리스트에 특정 권한이 포함되었는지 체크
	 * @param roles
	 * @param roleName
	 * @return
	 */
	public boolean hasRole(List<MemberRole> roles, String roleName) {
		
		for (MemberRole role : roles) {
			if (role.getRoleName().equals(roleName)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * member가 store에 대해 자격이 있는지 확인(store 매니저 또는 상위그룹의 멤버) 
	 * @param memberUid
	 * @param store
	 * @return
	 */
	public boolean hasStoreQualification(int memberUid, Store store) {
		Member member = getMember(memberUid);
		return hasStoreQualification(member, store);
	}
	
	/**
	 * member가 store에 대해 자격이 있는지 확인(store 매니저 또는 상위그룹의 멤버) 
	 * @param memberUid
	 * @param storeUid
	 * @return
	 */
	public boolean hasStoreQualification(int memberUid, int storeUid) {
		Member member = getMember(memberUid);
		Store store = storeService.getStore(storeUid);
		return hasStoreQualification(member, store);
	}

	/**
	 * member가 store에 대해 자격이 있는지 확인(store 매니저 또는 상위그룹의 멤버) 
	 * @param member
	 * @param store
	 * @return
	 */
	public boolean hasStoreQualification(Member member, Store store) {

		boolean qualified = false;
		
		if (member == null) return false;
		if (store == null) return false;
		
		// 해당 상점의 멤버인 경우
		if (member.getStore() != null && member.getStore().getUid() == store.getUid()) {
			qualified = true;
			
		// 그룹의 멤버인 경우 멤버가 접근 가능한 그룹 중 상점이 포함되었는지 확인
		} else if (member.getGroup() != null) {
			List<Group> groups = groupService.getGroups(member);
			
			outerLoop:
			for (Group g : groups) {
				for (Store s : g.getStores()) {
					if (s.getUid() == store.getUid()) {
						qualified = true;
						break outerLoop;
					}
				}
			}
		}
		
		return qualified;
	}
	
	public List<Store> getStores(Member member) {
		
		//boolean qualified = false;
		
		//if (member == null) return false;
		//if (store == null) return false;
		List<Store> stores=new ArrayList<Store>();
		// 해당 상점의 멤버인 경우
		if (member.getStore() != null) {
			stores.add(member.getStore());
			
			// 그룹의 멤버인 경우 멤버가 접근 가능한 그룹 중 상점이 포함되었는지 확인
		} else if (member.getGroup() != null) {
			List<Group> groups = groupService.getGroups(member);
			
			for (Group g : groups) {
				for (Store s : g.getStores()) {
					stores.add(s);
				}
			}
		}
		
		return stores;
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

	/**
	 * 아이디, 비밀번호로 멤버 찾기
	 * @param id
	 * @param password
	 * @return
	 */
	public Member getMember(String id, String password) {
		
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("ID can not be null.");
		}
		
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("Password can not be null.");
		}
		
		try {
			Member member = memberRepo.findById(id).get();
			String rawPassword = member.getPassword();
			if (pwEncoder.matches(password, rawPassword)) {
				return member;
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("ID or Password incorrect, please check ID and Password");
		}
		
		return null;
	}
	public Member getMember(String id) {		
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("ID can not be null.");
		}				
		Member member = memberRepo.findById(id).get();		
		return member;
	}
}
