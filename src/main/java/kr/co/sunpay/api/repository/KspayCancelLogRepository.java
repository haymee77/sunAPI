package kr.co.sunpay.api.repository;

import org.springframework.data.repository.CrudRepository;

import kr.co.sunpay.api.domain.KsnetCancelLog;

public interface KspayCancelLogRepository extends CrudRepository<KsnetCancelLog, Integer> {

}
