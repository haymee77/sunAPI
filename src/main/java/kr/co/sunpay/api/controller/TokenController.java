package kr.co.sunpay.api.controller;

import java.io.FileInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.FCMToken;
import kr.co.sunpay.api.service.MemberService;
import kr.co.sunpay.api.util.JwtTokenUtil;

@RestController
@RequestMapping("/auth")
public class TokenController {

	@Autowired
	JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	MemberService memberService;
	
	@Value("${fcm.key.path}")
	private String fcmKeyPath;
	
	@PostMapping("/token")
	public FCMToken fCMToken(@RequestBody FCMToken fCMToken) {
		
		Member member = null;
		fCMToken.setSuccess(false);
		
		// 멤버 검증 및 로그인 토큰 생성
		try {
			member = memberService.getMember(fCMToken.getId(), fCMToken.getPassword());
			
			if (member == null) {
				throw new IllegalArgumentException("Can not find ID and Password matching Member");
			} else {
				fCMToken.setLoginToken(jwtTokenUtil.generateToken(member));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException("Can not create token(err:" + ex.getMessage() + ")");
		}
		
		// fcmKey 확인용 PUSH 메세지 보내기
		try {
			FileInputStream serviceAccount = new FileInputStream(fcmKeyPath);
			
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://sunpay-sunshine.firebaseio.com/")
					.build();
			
			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}
			
		} catch (Exception ex) {
			System.out.println("FCM key file input stream excpetion");
			ex.printStackTrace();
		}
		
		Message message = Message.builder()
				.putData("title", "Sunpay")
				.putData("message", "FCM TOKEN 저장되었습니다.")
				.putData("isDisplay", "Y")
				.setToken(fCMToken.getFcmToken())
				.build();
		
		try {
			String response = FirebaseMessaging.getInstance().send(message);
			fCMToken.setFcmReturns(response);
			fCMToken.setSuccess(true);
			
		} catch (Exception ex) {
			System.out.println("## send massage failed");
			ex.printStackTrace();
			fCMToken.setFcmToken("");
		}
		
		return fCMToken;
	}
}
