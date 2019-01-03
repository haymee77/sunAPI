package kr.co.sunpay.api.repository;

import org.springframework.data.repository.CrudRepository;

import com.google.common.base.Optional;

import kr.co.sunpay.api.domain.StoreId;

public interface StoreIdRepository extends CrudRepository<StoreId, Integer> {

	public Optional<StoreId> findById(String id);
	public Optional<StoreId> findByIdAndActivated(String id, Boolean activated);
}
