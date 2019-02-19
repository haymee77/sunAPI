package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.DepositLog;

public interface DepositLogRepository extends JpaRepository<DepositLog, Integer> {

	List<DepositLog> findByDepositNo(String depositNo);
	
	Optional<DepositLog> findFirstByTrNoAndStatusCdOrderByCreatedDateDesc(String trNo, String statusCode);
	
	Optional<DepositLog> findOneByTrNoAndStatusCdOrderByCreatedDateDesc(String trNo, String statusCode);
}
