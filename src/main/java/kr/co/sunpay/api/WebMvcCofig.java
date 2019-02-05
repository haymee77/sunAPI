package kr.co.sunpay.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcCofig implements WebMvcConfigurer {

	private static final String[] httpInterceptorPath = {
		"/member", "/member/**"
		, "/deposit", "/deposit/**"
		, "/kspay", "/kspay/**"
		, "/git", "/git/**"
		, "/store", "/store/**"
	};
	
	@Autowired
	HttpInterceptor httpInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(httpInterceptor)
				.addPathPatterns(httpInterceptorPath);
				
	}
}
