package kr.co.sunpay.api.util;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class Util {

	public static String getHost(HttpServletRequest req) {
		try {
			String url = req.getRequestURL().toString();
			URI uri = new URI(url);
			return uri.getHost();
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
