package kr.co.sunpay.api.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import kr.co.sunpay.api.domain.Config;

public interface ConfigRepository extends CrudRepository<Config, Integer> {
	
	Optional<Config> findBySiteCode(String siteCode);
}
