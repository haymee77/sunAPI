package kr.co.sunpay.api.service;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import kr.co.sunpay.api.domain.KsnetCancelLog;
import kr.co.sunpay.api.domain.KsnetPayResult;

@Service
public class PushService {
	
	@Value("${fcm.key.path}")
	private static String fcmKeyPath;

	// KSNET 결제 결과 PUSH 
	public static void sendPush(KsnetPayResult ksnetPayResult) {
		
		// 결제 성공 시에만 PUSH 발솔
//		if (ksnetPayResult.getAuthyn().equals("O")) {
			Map<String, String> msg = new HashMap<String, String>();
			msg.put("cate", "paid");
			msg.put("isDisplay", "Y");
			msg.put("title", "상품 결제 완료");
			msg.put("message", ksnetPayResult.msgGenerator());
			
			push(msg);
//		}
	}
	
	public static void sendPush(KsnetCancelLog cancel) {
		
		Map<String, String> msg = new HashMap<String, String>();
		msg.put("cate", "paid");
		msg.put("isDisplay", "Y");
		msg.put("title", "상품 결제 완료");
		msg.put("message", cancel.msgGenerator());
		
		push(msg);
	}
	
	/**
	 * msg 필수 키값: cate, title, message, isDisplay
	 * @param msg
	 */
	public static void push(Map<String, String> msg) {
		// FCM Initialize
		try {
			FileInputStream serviceAccount = new FileInputStream(fcmKeyPath);

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// FCM PUSH
		String fcmToken = "ekTcwkuwJjk:APA91bEA6IGknujJJN_xJG_8ZZLwiCAxXX_FGU2-K5l7kbRKslJtiuzAP6cSyyp6QTTzT3ou1zLzbmdp9gVijTsNcxx4fLb0l0hrbtBgN2tykC09uFukXpXx7ATvWcZMLS_q_kcHDjPF";
		
		// Message 작성
		Message message = Message.builder()
				.putAllData(msg)
				.setToken(fcmToken)
				.build();
				
		try {
			String response = FirebaseMessaging.getInstance().send(message);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
