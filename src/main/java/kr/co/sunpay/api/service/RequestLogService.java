package kr.co.sunpay.api.service;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.RequestLog;
import kr.co.sunpay.api.repository.RequestLogRepository;

@Service
public class RequestLogService {
	
	@Autowired
	RequestLogRepository logRepo;

	public void saveLog(HttpServletRequest request) throws UnsupportedEncodingException {

		RequestLog log = new RequestLog();
		String headers = "";
		String hName = "";
		String hValue = "";

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			hName = headerNames.nextElement();
			hValue = request.getHeader(hName);
			headers += "Header Name: " + hName + ", Value: " + hValue + "\n";
		}

		if (headers.length() > 1000)
			headers = headers.substring(0, 1000);

		log.setHeader(headers);
		log.setMethod(request.getMethod());
		log.setIp(request.getRemoteAddr());
		log.setUri(request.getRequestURI());
		
		logRepo.save(log);
	}
}
