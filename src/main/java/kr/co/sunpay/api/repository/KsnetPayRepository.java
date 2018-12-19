package kr.co.sunpay.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.KsnetPay;

public interface KsnetPayRepository extends JpaRepository<KsnetPay, Integer> {
	KsnetPay findByUid(int uid);
}
