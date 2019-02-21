package kr.co.sunpay.api.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import kr.co.sunpay.api.domain.KsnetRefundLog;

public interface KspayRefundLogRepository extends CrudRepository<KsnetRefundLog, Integer> {

	Optional<KsnetRefundLog> findByTrNoAndRStatus(String trNo, String rStatus);
}
