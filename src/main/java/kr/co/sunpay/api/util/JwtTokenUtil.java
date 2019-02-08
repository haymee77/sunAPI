package kr.co.sunpay.api.util;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kr.co.sunpay.api.domain.Member;

@Component
public class JwtTokenUtil implements Serializable {
	
	private static final long serialVersionUID = 2631341913206442639L;
	public static final long DEFAULT_TOKEN_VALIDITY_SECONDS = 60 * 60;
	public static final String SIGNING_KEY = "devglan123r";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";

	public String generateToken(Member member) {
		
		Claims claims = Jwts.claims().setSubject(member.getId());
		
		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuer("Sunpay - API")
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + DEFAULT_TOKEN_VALIDITY_SECONDS * 24 * 30))
				.signWith(key).compact();
	}
}
