package kr.co.sunpay.api.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import kr.co.sunpay.api.exception.ErrorDetails;
import kr.co.sunpay.api.util.Util;
import kr.co.sunpay.common.LocalDateTimeJsonConverter;
import lombok.extern.java.Log;

@Log
public class ApiKeyFilter extends GenericFilterBean {

	private String headerKeyName;
	
	private static final List<String> FILTER_WHITELIST = Arrays.asList(
			"/swagger-ui.html", 
			"/swagger-resources",
			"/webjars", 
			"/v2/api-docs",
			"/ksnet",
			"/error",
			"/login",
			"/git");
	
	public ApiKeyFilter(String headerKeyName) {
		this.headerKeyName = headerKeyName;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		log.info("## ApiKeyFilter...");

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String path = httpRequest.getServletPath();

		// FILTER_WHITELIST에 포함되지 않는 path인 경우 - API KEY 검사
		if (path != null && !FILTER_WHITELIST.stream().anyMatch(str -> path.contains(str))) {
			if (!authApiKey(httpRequest)) {
				ErrorDetails error = new ErrorDetails(HttpStatus.UNAUTHORIZED,
						"The API key was not found or not the expected value.");
				Gson gson = new GsonBuilder()
								.setPrettyPrinting()
								.serializeNulls()
								.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonConverter())
								.create();

				httpResponse.setContentType("application/json");
				httpResponse.setCharacterEncoding("UTF-8");
				httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				httpResponse.getWriter().write(gson.toJson(error));
				return;
			}
		}

		chain.doFilter(request, response);
	}

	/**
	 * 요청 데이터로 권한 검증
	 * @param request
	 * @return
	 */
	private boolean authApiKey(HttpServletRequest request) {
	
		List<String> allowedDomain = Arrays.asList(
				"sunpay.co.kr", 
				"test.sunpay.co.kr", 
				"m.sunpay.co.kr", 
				"m.test.sunpay.co.kr", 
				"shop.sunpay.co.kr", 
				"dev.sunpay.co.kr");
		String apiKey = request.getHeader(headerKeyName);
		String hostname = Util.getHost(request);
		
		System.out.println("## api hostname check");
		System.out.println(hostname);
		
		// TODO: 검증 로직에 대한 설계 필요.. 일단 임시로 하드코딩해둠..
		if ("qscEsZ56WE@#55ygwu7*65tGskek@ejK".equals(apiKey) || "iSunp".equals(apiKey) || "1111".equals(apiKey)) {
			return true;
		}
		
		if (allowedDomain.contains(hostname)) {
			return true;
		}
		
		return false;
	}
}