package kr.co.sunpay.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import kr.co.sunpay.api.service.RequestLogService;
import lombok.extern.java.Log;

@Log
@Component
public class HttpInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	RequestLogService logService;
	
	/**
	 * Controller 전처리
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		log.info("## HttpInterceptor.preHandle called..");
		logService.saveLog(request);
		
		return true;
	}

	/** 
	 * Controller 후처리
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
	}
}
