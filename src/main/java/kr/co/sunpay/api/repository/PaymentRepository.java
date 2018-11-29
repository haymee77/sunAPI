package kr.co.sunpay.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.co.sunpay.api.domain.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
	
	public Optional<Payment> findByUid(int uid);
	
	public List<Payment> findByStoreId(String storeId);
	
	public Optional<Payment> findByStoreIdAndOrderNo(String storeId, String orderNo);
}
