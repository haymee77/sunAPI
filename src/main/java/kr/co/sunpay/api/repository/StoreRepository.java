package kr.co.sunpay.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

	Optional<Store> findByUid(int uid);
}
