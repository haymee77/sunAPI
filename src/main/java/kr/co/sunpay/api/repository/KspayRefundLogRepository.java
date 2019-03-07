package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import kr.co.sunpay.api.domain.KsnetRefundLog;

public interface KspayRefundLogRepository extends CrudRepository<KsnetRefundLog, Integer> {

	Optional<KsnetRefundLog> findByTrNoAndRStatus(String trNo, String rStatus);
	List<KsnetRefundLog> findByTrNoAndStatusCodeNot(String trNo, String statusCode);
}
