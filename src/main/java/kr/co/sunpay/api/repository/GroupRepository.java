package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {

	List<Group> findByparentGroupUid(int parentGroupUid);
	Optional<Group> findByUid(int uid);
	Optional<Group> findByRoleCode(String code);
	
	// 대리점 리스트 (2019-06-19:JAEROX)
	List<Group> findByRoleCodeAndDeleted(String roleCode, Boolean deleted);
			
	// 대리점 연동 리스트 (2019-06-19:JAEROX)
	List<Group> findByRoleCodeAndApiAgencyYn(String roleCode, Boolean apiAgencyYn);
		
	// 지사 소속 대리점 리스트 (2019-06-19:JAEROX)
	List<Group> findByRoleCodeAndParentGroupUid(String roleCode, int parentGroupUid);
		
	// 지사 소속 연동대리점 리스트 (2019-06-19:JAEROX)
	List<Group> findByRoleCodeAndParentGroupUidAndApiAgencyYn(String roleCode, int parentGroupUid, Boolean apiAgencyYn);
}
