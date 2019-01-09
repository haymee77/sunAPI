package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.co.sunpay.api.domain.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

	Optional<Store> findByUid(int uid);

	@Query(value="SELECT * FROM SP_STORES WHERE GROUP_UID_FK IN ("
			+ "SELECT UID FROM SP_GROUPS WHERE PARENT_GROUP_UID=:groupUid OR UID=:groupUid)"
			+ " AND DELETED <> 1", nativeQuery=true)
	List<Store> findByGroup(@Param("groupUid") int groupUid);
}
