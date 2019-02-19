package kr.co.sunpay.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.DepositLog;

public interface DepositLogRepository extends JpaRepository<DepositLog, Integer> {

	List<DepositLog> findByDepositNo(String depositNo);
}
