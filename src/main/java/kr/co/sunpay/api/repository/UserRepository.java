package kr.co.sunpay.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.sunpay.api.domain.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	
	public Optional<User> findByUid(int uid);
	
	public Optional<User> findByUsername(String username);

}
