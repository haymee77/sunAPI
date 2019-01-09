package kr.co.sunpay.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.co.sunpay.api.domain.KsnetPayResult;

@Repository
public interface KsnetPayResultRepository extends JpaRepository<KsnetPayResult, Integer> {

	List<KsnetPayResult> findByStoreIdIn(List<String> storeIds);
}
