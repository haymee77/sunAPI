package kr.co.sunpay.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcCofig implements WebMvcConfigurer {

	private static final String[] httpExcludePath = {
			"/swagger-ui.html"
			, "/swagger-resources", "/swagger-resources/**"
			, "/webjars", "/webjars/**" 
			, "/v2/api-docs", "/v2/api-docs/**"
	};
	
	@Autowired
	HttpInterceptor httpInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(httpInterceptor)
				.excludePathPatterns(httpExcludePath);
				
	}
}
