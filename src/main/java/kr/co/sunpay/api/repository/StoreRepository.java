package kr.co.sunpay.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

}
