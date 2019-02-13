package kr.co.sunpay.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.User;
import kr.co.sunpay.api.repository.UserRepository;

@Service
public class ApiUserService implements UserDetailsService {

	@Autowired
	private UserRepository repo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = repo.findByUsername(username).orElse(null);
		
		System.out.println(user.getUsername());
		
		return repo.findByUsername(username)
					.filter(m -> m != null)
					.map(m -> new ApiUserDetail(m)).get();
	}
}
