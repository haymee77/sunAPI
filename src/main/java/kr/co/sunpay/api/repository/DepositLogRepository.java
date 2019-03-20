package kr.co.sunpay.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.DepositLog;

public interface DepositLogRepository extends JpaRepository<DepositLog, Integer> {

	List<DepositLog> findByDepositNo(String depositNo);
	
	List<DepositLog> findByDepositNoOrderByCreatedDateDesc(String depositNo);
	
	List<DepositLog> findByDepositNoAndCreatedDateBetweenOrderByCreatedDateDesc(String depositNo, LocalDateTime createdDateStart, LocalDateTime createdDateeEnd);
	
	List<DepositLog> findByDepositNoAndCreatedDateBetweenAndTypeCodeOrderByCreatedDateDesc(String depositNo, LocalDateTime createdDateStart, LocalDateTime createdDateeEnd, String typeCode);
	
	Optional<DepositLog> findFirstByTrNoAndStatusCdOrderByCreatedDateDesc(String trNo, String statusCode);
	
	Optional<DepositLog> findOneByTrNoAndStatusCdOrderByCreatedDateDesc(String trNo, String statusCode);
}
