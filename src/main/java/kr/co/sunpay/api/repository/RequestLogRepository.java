package kr.co.sunpay.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.RequestLog;

public interface RequestLogRepository extends JpaRepository<RequestLog, Integer> {

}
