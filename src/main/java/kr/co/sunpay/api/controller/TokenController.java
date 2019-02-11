package kr.co.sunpay.api.controller;

import java.io.FileInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.repository.FcmTokenRepository;
import kr.co.sunpay.api.domain.FcmToken;
import kr.co.sunpay.api.service.MemberService;
import kr.co.sunpay.api.util.JwtTokenUtil;

@RestController
@RequestMapping("/auth")
public class TokenController {

	@Autowired
	JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	private FcmTokenRepository fcmTokenRepo;
	
	@Value("${fcm.key.path}")
	private String fcmKeyPath;
	
	/**
	 * ID, Password, FCM Token 받아서 검증 후 로그인 토큰 리턴
	 * @param fcmToken
	 * @return
	 */
	@PostMapping("/token")
	public FcmToken fcmToken(@RequestBody FcmToken fcmToken) {
		
		Member member = null;
		FcmToken dbToken;
		fcmToken.setSuccess(false);
		
		// 멤버 검증 및 로그인 토큰 생성
		try {
			member = memberService.getMember(fcmToken.getId(), fcmToken.getPassword());
			
			if (member == null) {
				throw new IllegalArgumentException("Can not find ID and Password matching Member");
			} else {
				// 로그인 토큰 생성
				String loginToken = jwtTokenUtil.generateToken(member);
				fcmToken.setLoginToken(loginToken);
				
				// 기 FCM Token 있는지 확인
				dbToken = fcmTokenRepo.findById(fcmToken.getId()).orElse(fcmToken);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			fcmToken.setFcmReturns("Can not create token(err:" + ex.getMessage() + ")");
			return fcmToken;
		}
		
		// fcmKey 확인용 PUSH 메세지 보내기
		try {
			FileInputStream serviceAccount = new FileInputStream(fcmKeyPath);
			
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();
			
			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fcmToken.setFcmReturns("FCM key file input stream excpetion)");
			return fcmToken;
		}
		
		Message message = Message.builder()
				.putData("title", "Sunpay")
				.putData("message", "FCM TOKEN 저장되었습니다.")
				.putData("isDisplay", "Y")
				.setToken(fcmToken.getFcmToken())
				.build();
		
		try {
			String response = FirebaseMessaging.getInstance().send(message);
			fcmToken.setFcmReturns(response);
			fcmToken.setSuccess(true);
			
			// fcmToken 갱신 및 저장
			dbToken.setFcmReturns(fcmToken.getFcmReturns());
			dbToken.setSuccess(fcmToken.getSuccess());
			dbToken.setFcmToken(fcmToken.getFcmToken());
			fcmTokenRepo.save(dbToken);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fcmToken.setFcmReturns("PUSH Faild, please check FCM Token.");

			// fcmToken 갱신 및 저장
			dbToken.setFcmReturns(fcmToken.getFcmReturns());
			dbToken.setSuccess(fcmToken.getSuccess());
			dbToken.setFcmToken(fcmToken.getFcmToken());
			fcmTokenRepo.save(dbToken);
			
			return fcmToken;
		}
		
		return fcmToken;
	}
	
	@ApiModelProperty(notes="유효한 토큰인 경우 멤버 ID 리턴")
	@GetMapping("/token")
	public Claims authToken(@RequestParam("token") String token) {
		if (token == null || token.isEmpty()) {
			return null;
		}
		
		try {
			return jwtTokenUtil.verifyToken(token);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
		
	}
}
