package kr.co.sunpay.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.co.sunpay.api.domain.Code;

public interface CodeRepository extends JpaRepository<Code, Integer> {
	
	public static final String FIND_GROUP_LIST = "SELECT GROUP_NM FROM SP_CODES GROUP BY GROUP_NM";
	
	List<Code> findByGroupName(String groupName);
	int countByGroupName(String groupName);
	
	@Query(value=FIND_GROUP_LIST, nativeQuery=true)
	List<String> findGroupList();
}
