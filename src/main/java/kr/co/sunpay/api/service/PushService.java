package kr.co.sunpay.api.service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import kr.co.sunpay.api.domain.FcmToken;
import kr.co.sunpay.api.domain.KsnetCancelLog;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.repository.FcmTokenRepository;
import kr.co.sunpay.api.repository.StoreIdRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class PushService {

	@Value("${fcm.key.path}")
	private String fcmKeyPath;

	@Autowired
	StoreRepository storeRepo;

	@Autowired
	StoreIdRepository storeIdRepo;

	@Autowired
	FcmTokenRepository fcmTokenRepo;

	// FCM 토큰 테스트
	public boolean sendTest(String fcmToken, Map<String, String> msg) {
		// FCM Initialize
		try {
			FileInputStream serviceAccount = new FileInputStream(getFcmKeyPath());

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Message 작성
		Message message = Message.builder().putAllData(msg).setToken(fcmToken).build();

		try {
			FirebaseMessaging.getInstance().send(message);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	// KSNET 결제 결과 PUSH
	public void sendPush(KsnetPayResult ksnetPayResult) {

		// 상점 ID로 수신자 조회
		List<String> tokens = getTokenByStoreId(ksnetPayResult.getStoreId());
		
		Map<String, String> msg = new HashMap<String, String>();
		msg.put("cate", "paid");
		msg.put("isDisplay", "Y");
		msg.put("title", "결제알림");
		msg.put("message", ksnetPayResult.msgGenerator());
		
		tokens.forEach(token -> {
			push(token, msg);
		});
	}

	/**
	 * 결제 취소 시 PUSH
	 * 
	 * @param cancel
	 */
	public void sendPush(KsnetCancelLog cancel) {

		// 상점 ID로 수신자 조회
		List<String> tokens = getTokenByStoreId(cancel.getStoreId());

		Map<String, String> msg = new HashMap<String, String>();
		msg.put("cate", "refund");
		msg.put("isDisplay", "Y");
		msg.put("title", "환불알림");
		msg.put("message", cancel.msgGenerator());

		tokens.forEach(token -> {
			push(token, msg);
		});
	}

	/**
	 * msg 필수 키값: cate, title, message, isDisplay
	 * 
	 * @param msg
	 */
	public boolean push(String fcmToken, Map<String, String> msg) {

		// FCM Initialize
		try {
			FileInputStream serviceAccount = new FileInputStream(getFcmKeyPath());

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// FCM PUSH(devstore1)
//		fcmToken = "ekTcwkuwJjk:APA91bEA6IGknujJJN_xJG_8ZZLwiCAxXX_FGU2-K5l7kbRKslJtiuzAP6cSyyp6QTTzT3ou1zLzbmdp9gVijTsNcxx4fLb0l0hrbtBgN2tykC09uFukXpXx7ATvWcZMLS_q_kcHDjPF1";

		// Message 작성
		Message message = Message.builder().putAllData(msg).setToken(fcmToken).build();

		try {
			FirebaseMessaging.getInstance().send(message);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	public List<String> getTokenByStoreId(String id) {
		
		System.out.println("## PushService");
		System.out.println("상점ID로 조회: " + id);
		
		List<String> tokens = new ArrayList<String>();

		// 상점ID로 상점 조회 > 상점의 멤버 > FCM TOKEN 조회
		StoreId storeId = storeIdRepo.findById(id).orElse(null);
		Store store = storeRepo.findByStoreIds(storeId).orElse(null);
		FcmToken fcmToken;

		for (Member member : store.getMembers()) {
			fcmToken = fcmTokenRepo.findById(member.getId()).orElse(null);

			if (fcmToken != null) {
				tokens.add(fcmToken.getFcmToken());
			}

		}

		return tokens;
	}
}
