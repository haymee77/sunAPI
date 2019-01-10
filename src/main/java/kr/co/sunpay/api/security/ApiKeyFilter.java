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
			"/git");

	public ApiKeyFilter(String headerKeyName) {
		this.headerKeyName = headerKeyName;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		log.info("...ApiKeyFilter...");

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String path = httpRequest.getServletPath();

		// FILTER_WHITELIST에 포함되지 않는 path인 경우 - API KEY 검사
		if (path != null && !FILTER_WHITELIST.stream().anyMatch(str -> path.contains(str))) {
			String apiKey = httpRequest.getHeader(headerKeyName);
			log.info("-- api key: " + apiKey);
			if (!"1111".equals(apiKey)) {
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
			} else {
				log.info("-- API KEY ERROR!!!");
			}
		}

		chain.doFilter(request, response);
	}
}