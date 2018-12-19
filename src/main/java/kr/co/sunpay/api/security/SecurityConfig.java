package kr.co.sunpay.api.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import lombok.extern.java.Log;

@Log
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		log.info("-- SecurityConfig.configure called...");
		
		// 페이지 접근 권한 제어
		http.authorizeRequests()
			.antMatchers("/**").permitAll();
		
		http.csrf().disable();
		http.headers().disable();
		
		// X-Frame-Options 설정
		http.antMatcher("/ksnet/**")
			.headers()
			.frameOptions()
			.disable();
	}
}
