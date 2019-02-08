package kr.co.sunpay.api.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import kr.co.sunpay.api.domain.FcmToken;

public interface FcmTokenRepository extends CrudRepository<FcmToken, Integer> {

	Optional<FcmToken> findById(String id);
}
