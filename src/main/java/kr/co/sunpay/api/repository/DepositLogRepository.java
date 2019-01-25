package kr.co.sunpay.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.DepositLog;

public interface DepositLogRepository extends JpaRepository<DepositLog, Integer> {

}
