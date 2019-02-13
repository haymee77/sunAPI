package kr.co.sunpay.api.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import kr.co.sunpay.api.security.ApiUserService;
import lombok.extern.java.Log;

@Log
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
    private ApiUserService apiUserService;
	
	@Value("${api.key-header-name}")
	private String headerKeyName;
	
	private static final String[] AUTH_USER = {
		"/swagger-resources/**",
		"/swagger-ui.html",
		"/v2/api-docs"
	};
	
	// 로그인 패스하는 URL
	private static final String[] AUTH_PASS = {
			"/ksnet/**",
			"/kspay/**"
	};
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**", "/webjars/**", "/js/**", "/css/**", "/img/**");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		log.info("-- SecurityConfig.configure called...");
		
		// Custom Filter 추가
		http.addFilterAfter(new ApiKeyFilter(headerKeyName), BasicAuthenticationFilter.class);
		
		// 페이지 접근 권한 제어
		http.authorizeRequests()
			.antMatchers(AUTH_PASS).permitAll()
			.antMatchers(AUTH_USER).hasRole("USER");
		
		// Swagger-ui 로그인
		http.formLogin().loginPage("/login");
		http.exceptionHandling().accessDeniedPage("/accessDenied");
		http.logout().invalidateHttpSession(true);
		
		CorsConfiguration cors = new CorsConfiguration();
		cors.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE"));
		
		http.csrf().disable()
			.cors().configurationSource(request -> new CorsConfiguration(cors).applyPermitDefaultValues());
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(apiUserService).passwordEncoder(passwordEncoder());
	}
}