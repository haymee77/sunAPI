package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {

	List<Group> findByparentGroupUid(int parentGroupUid);
	Optional<Group> findByUid(int uid);
}
