package kr.co.sunpay.api.model;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.DepositLog;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import kr.co.sunpay.api.repository.DepositLogRepository;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
import kr.co.sunpay.api.repository.StoreIdRepository;
import kr.co.sunpay.api.repository.StoreRepository;
import kr.co.sunpay.api.service.CodeService;
import kr.co.sunpay.api.service.PushService;
import kr.co.sunpay.api.service.StoreService;

@Service
public class DepositService extends CodeService {

	@Autowired
	StoreRepository storeRepo;
	
	@Autowired
	StoreIdRepository storeIdRepo;
	
	@Autowired
	DepositLogRepository depositLogRepo;
	
	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;
	
	@Autowired
	PushService pushService;
	
	@Autowired
	StoreService storeService;
	
	public static final String TYPE_DEPOSIT = "DEPOSIT";		// 입금
	public static final String TYPE_WITHDRAW = "WITHDRAW";		// 출금
	
	public static final String STATUS_WAITING = "DEPOSIT_WAITING";	// 입금 대기중(입금번호 오류인 경우)
	public static final String STATUS_FINISH = "DEPOSIT_FINISH";	// 입금 완료		
	public static final String STATUS_TRY = "REFUND_TRY";			// 예치금 차감 시도
	public static final String STATUS_COMPLETE = "REFUND_COMPLETE";	// 예치금 차감 완료
	public static final String STATUS_FAIL = "REFUND_FAIL";			// 예치금 차감 실패
	
	public static final String LOG_TYPE_CD_GROUP_NM = "DEPOSIT_TYPE";
	public static final String LOG_STATUS_CD_GROUP_NM = "DEPOSIT_STATUS";
	
	public Map<String, String> TYPE_MAP = null;
	public Map<String, String> STATUS_MAP = null;
	
	/**
	 * depositNo로 상점 검색해서 예치금 증액 및 히스토리 기록
	 * @param depositNo
	 * @param depositAmt
	 */
	public void deposit(String depositNo, int depositAmt) {
		
		Store store = storeRepo.findByDepositNo(depositNo).orElse(null);
		if (store == null) throw new EntityNotFoundException("상점을 찾을 수 없습니다.");
		
		store.setDeposit(store.getDeposit() + depositAmt);
		storeRepo.save(store);
		writeLog(store, depositNo, store.getDepositNo(), DepositService.TYPE_DEPOSIT, null, DepositService.STATUS_FINISH, depositAmt);
	}
	
	/**
	 * 예치금 입금 PUSH 발송
	 * @param depositNo
	 * @param depositAmt
	 */
	public void sendDepositAddPush(String depositNo, int depositAmt) {
		
		Store store = storeRepo.findByDepositNo(depositNo).orElse(null);
		
		if (store != null) {
			List<String> tokens = pushService.getTokensByStore(store);
			
			if (tokens.size() > 0) {
				Map<String, String> msg = new HashMap<String, String>();
				String msgText = "[예치금입금]"
						+ "\n입금액: " + NumberFormat.getNumberInstance(Locale.US).format(depositAmt) + "원"
						+ "\n잔액: " + NumberFormat.getNumberInstance(Locale.US).format(store.getDeposit()) + "원";
				msg.put("cate", "deposit");
				msg.put("isDisplay", "Y");
				msg.put("title", "예치금 입금");
				msg.put("message", msgText);

				tokens.forEach(token -> {
					pushService.push(token, msg);
				});
			}
		}
	}
	/**
	 * 입금번호로 상점 검색하여 유효한 입금번호인지 확인
	 * @param depositNo
	 * @return
	 */
	public boolean isValidNo(String depositNo) {
		
		Optional<Store> oStore = storeRepo.findByDepositNo(depositNo);
		if (!oStore.isPresent()) return false;
		
		return true;
	}
	
	/**
	 * 취소 요청 바디 받아서 예치금 임시 차감 및 결제 취소 로그 기록 
	 * @param cancelBody
	 * @throws Exception
	 */
	public void tryRefund(KspayCancelBody cancel) throws Exception {
		
		// 예치금 차감할 상점 검색
		StoreId storeId = storeIdRepo.findById(cancel.getStoreid()).orElse(null);
		Store store = storeRepo.findByStoreIds(storeId).orElse(null);
		
		if (store == null) {
			throw new Exception("상점 정보를 찾을 수 없음");
		}
		
		// 주문번호, 상점ID로 결제금액 검색
		Optional<KsnetPayResult> oPayResult = ksnetPayResultRepo.findByTrnoAndStoreIdAndAuthyn(cancel.getTrno(), cancel.getStoreid(), "O");
		if (!oPayResult.isPresent()) {
			throw new Exception("결제 정보를 찾을 수 없음");
		}
		
		int paidAmount = oPayResult.get().getAmt();
		if (store.getDeposit() < paidAmount) {
			throw new Exception("취소예치금 부족");
		}
		
		store.setDeposit(store.getDeposit() - paidAmount);
		storeRepo.save(store);
		writeLog(store, null, null, DepositService.TYPE_WITHDRAW, cancel.getTrno(), DepositService.STATUS_TRY, paidAmount);
	}
	
	
	public void completeRefund(KspayCancelBody cancel) throws Exception {
		DepositLog log = depositLogRepo.findFirstByTrNoAndStatusCdOrderByCreatedDateDesc(cancel.getTrno(), DepositService.STATUS_TRY).orElse(null);
		
		if (log == null) {
			throw new Exception("취소 요청내역 찾을 수 없음");
		}

		log.setStatusCd(DepositService.STATUS_COMPLETE);
	}
	
	public void writeLog(Store store, String originalDepositNo, String depositNo, String typeCode, String trNo, String statusCode, int amt) {
		
		int deposit = 0;
		
		if (store != null) {
			deposit = store.getDeposit();
			
			if (depositNo == null) depositNo = store.getDepositNo();
		}
		
		DepositLog log = new DepositLog(store, originalDepositNo, depositNo, typeCode, trNo, statusCode, amt, deposit);
		depositLogRepo.save(log);
	}

	/** 
	 * 결제 취소 실패 시 보증금 원복
	 * @param cancelBody
	 */
	public void resetDeposit(KspayCancelBody cancel) throws Exception {
		
		// 예치금 복구할 상점 검색
		Optional<StoreId> oStoreId = storeIdRepo.findById(cancel.getStoreid());
		if (!oStoreId.isPresent()) {
			throw new Exception("존재하지 않는 상점 ID");
		}

		Optional<Store> oStore = storeRepo.findByUid(oStoreId.get().getStore().getUid());
		if (!oStore.isPresent()) {
			throw new Exception("상점 정보를 찾을 수 없음");
		}
		Store store = oStore.get();

		// 주문번호, 상점ID로 결제금액 검색
		Optional<KsnetPayResult> oPayResult = ksnetPayResultRepo.findByTrnoAndStoreIdAndAuthyn(cancel.getTrno(),
				cancel.getStoreid(), "O");
		if (!oPayResult.isPresent()) {
			throw new Exception("결제 정보를 찾을 수 없음");
		}

		int paidAmount = oPayResult.get().getAmt();
		System.out.println("RESET DEPOSIT AMOUNT:" + paidAmount);

		store.setDeposit(store.getDeposit() + paidAmount);
		storeRepo.save(store);
		writeLog(store, null, null, DepositService.TYPE_DEPOSIT, cancel.getTrno(), DepositService.STATUS_FAIL,
				paidAmount);
		
	}
	
	/**
	 * 예치금 내역 리턴
	 * @param memberUid
	 * @param depositNo
	 * @return
	 */
	public List<DepositLog> getLogs(int memberUid, String depositNo) {
		List<DepositLog> logs = new ArrayList<DepositLog>();
		
		// memberUid 권한 확인
		Store store = storeRepo.findByDepositNo(depositNo).orElse(null);
		if (store == null) throw new IllegalArgumentException("Can not find store information.");
		if (!storeService.hasStoreQualification(memberUid, store)) throw new BadCredentialsException("Permission Denied.");
		
		// DepositNo 로 검색하여 리턴
		logs = depositLogRepo.findByDepositNo(depositNo);
		for (DepositLog log : logs) {
			wrappingLog(log);
		}
		
		return logs;
	}
	
	/**
	 * 예치금 내역 리턴
	 * @param memberUid
	 * @param depositNo
	 * @param type
	 * @return
	 */
	public List<DepositLog> getLogs(int memberUid, String depositNo, String type) {
		
		List<DepositLog> logs = new ArrayList<DepositLog>();
		List<DepositLog> filteredLogs = new ArrayList<DepositLog>();
		
		if (type == null || type.isEmpty()) return getLogs(memberUid, depositNo);
		
		return logs;
	}
	
	public DepositLog wrappingLog(DepositLog log) {
		
		if (TYPE_MAP == null) {
			TYPE_MAP = getCodeMap(LOG_TYPE_CD_GROUP_NM);
		}
		
		if (STATUS_MAP == null) {
			STATUS_MAP = getCodeMap(LOG_STATUS_CD_GROUP_NM);
		}
	
		// 코드값 변환
		log.setType(TYPE_MAP.get(log.getTypeCd()));
		log.setStatus(STATUS_MAP.get(log.getStatusCd()));
		
		// 금액 천단위 표기
		log.setFormatAmt(NumberFormat.getNumberInstance(Locale.US).format(log.getAmt()));
		log.setFormatTotal(NumberFormat.getNumberInstance(Locale.US).format(log.getTotal()));
		
		return log;
	}
}
