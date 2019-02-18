package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;

public interface StoreRepository extends JpaRepository<Store, Integer> {

	Optional<Store> findByUid(int uid);

	Optional<Store> findByDepositNo(String depositNo);

	@Query("SELECT s.uid, s.bizOwner, s.bizName FROM Store s WHERE s.bizOwner LIKE ?1%")
	List<Object[]> findByBizOwner(String bizOwner);

	@Query(value = "SELECT UID, BIZ_NO, BIZ_OWNER, BIZ_NM FROM SP_STORES WHERE BIZ_NO=:bizNo", nativeQuery = true)
	List<Object[]> findByBizNo(@Param("bizNo") String bizNo);
	
	Optional<Store> findByStoreIds(StoreId storeId);
}
