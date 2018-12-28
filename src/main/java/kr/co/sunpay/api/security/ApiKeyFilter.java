package kr.co.sunpay.api.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.GenericFilterBean;

import lombok.extern.java.Log;

@Log
public class ApiKeyFilter extends GenericFilterBean {
	
	private String headerKeyName;
	
	private static final List<String> FILTER_WHITELIST = Arrays.asList(
		"/swagger-ui.html"
		, "/swagger-resources"
		, "/webjars"
		, "/v2/api-docs"
	);
	
	public ApiKeyFilter(String headerKeyName) {
		this.headerKeyName = headerKeyName;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		log.info("...ApiKeyFilter...");
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String path = httpRequest.getServletPath();
		
		// FILTER_WHITELEST에 포함되지 않는 path인 경우
		if (path != null && !FILTER_WHITELIST.stream().anyMatch(str -> path.contains(str))) {
			log.info("...come...");
			log.info(headerKeyName);
			String apiKey = httpRequest.getHeader(headerKeyName);
		}
		
		chain.doFilter(request, response);
	}
}
