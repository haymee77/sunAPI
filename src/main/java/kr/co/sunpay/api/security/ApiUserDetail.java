package kr.co.sunpay.api.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import kr.co.sunpay.api.domain.User;
import kr.co.sunpay.api.domain.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiUserDetail extends org.springframework.security.core.userdetails.User {
	
	private static final String ROLE_PREFIX = "ROLE_";
	private User user;
	
	public ApiUserDetail(User user) {
		super(user.getUsername(), user.getPassword(), makeGrantedAuthority(user.getRoles()));
		this.user = user;
	}
	
	public static List<GrantedAuthority> makeGrantedAuthority(List<UserRole> roles) {
		
		List<GrantedAuthority> list = new ArrayList<>();
		
		roles.forEach(
				role -> list.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.getRoleName())));
		
		return list;
	}

}
