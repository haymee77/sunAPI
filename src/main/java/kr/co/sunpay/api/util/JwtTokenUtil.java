package kr.co.sunpay.api.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import kr.co.sunpay.api.domain.Member;

@Component
public class JwtTokenUtil {
	
	private static final String SECRET_KEY = "5Wjd#qKr8gK@dhS*";
	
	public static final long TOKEN_FOR_MIN = 1000 * 60;
	public static final long TOKEN_FOR_DAY = 1000 * 60 * 60 * 24;
	public static final String TOKEN_PREFIX = "Sunpay ";
	public static final String HEADER_STRING = "Authorization";

	public String generateToken(Member member) {
		
		// 토큰 만료일 - 5분
		Date expiredTime = new Date();
		expiredTime.setTime(expiredTime.getTime() + TOKEN_FOR_MIN * 5);
		
		// 암호화
		SignatureAlgorithm alg = SignatureAlgorithm.HS256;
		byte[] secretKey = DatatypeConverter.parseBase64Binary(SECRET_KEY);
		Key signingKey = new SecretKeySpec(secretKey, alg.getJcaName());
		
		// 토큰 Header
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("typ", "JWP");
		header.put("alg", "HS256");
		
		// Claims
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("username", member.getId());
		claims.put("email", member.getEmail());
		
		return Jwts.builder()
				.setHeader(header)
				.setClaims(claims)
				.setExpiration(expiredTime)
				.signWith(signingKey)
				.compact();
	}

	public String verifyToken(String jwt) {
		
		try {
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
					.parseClaimsJws(jwt).getBody();
			
			return (String) claims.get("username");
			
		} catch (ExpiredJwtException ex) {
			throw new IllegalArgumentException("토큰 만료됨");
		} catch (JwtException ex) {
			throw new IllegalArgumentException("토큰 변조됨");
		} catch (Exception ex) {
			throw new IllegalArgumentException("토큰 검증 오류");
		}
		
	}
}