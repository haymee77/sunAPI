package kr.co.sunpay.api.service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.domain.KsnetRefundLog;
import kr.co.sunpay.api.exception.DepositException;
import kr.co.sunpay.api.domain.KsnetPayResult;
import kr.co.sunpay.api.model.DepositService;
import kr.co.sunpay.api.model.KsnetRefundBody;
import kr.co.sunpay.api.model.KspayRefundReturns;
import kr.co.sunpay.api.repository.KsnetPayResultRepository;
import kr.co.sunpay.api.repository.KsnetRefundLogRepository;
import kr.co.sunpay.api.util.Sunpay;
import ksnet.kspay.KSPayApprovalCancelBean;
import lombok.extern.java.Log;

@Log
@Service
public class KsnetService {

	@Autowired
	DepositService depositService;
	
	@Autowired
	StoreService storeService;
	
	@Autowired
	PushService pushService;
	
	@Autowired
	KsnetRefundLogRepository refundLogRepo;
	
	@Autowired
	KsnetPayResultRepository ksnetPayResultRepo;

	// 소켓통신(KSPAY 소켓 설치된 서버의 IP, PORT) 
	public static final String IPG_IP_ADDR = "210.181.28.137";
	public static final int IPG_PORT = 21001;

	public static final String KSPAY_AUTHTY_CREDIT = "1010";
	public static final String KSPAY_AUTHTY_BANK_CANCEL = "2010";
	public static final String KSPAY_AUTHTY_BANK_REFUND = "2030";
	public static final String KSPAY_AUTHTY_MOBILE = "M110";
	
	public static final String KSPAY_CANCEL_RSTATUS_TRUE = "O";

	/**
	 * 결제 취소 요청건 저장
	 * 
	 * @param cancelBody
	 */
	public KsnetRefundLog saveRefundLog(KsnetRefundBody refund) {

		KsnetRefundLog log = new KsnetRefundLog(refund.getStoreid(), refund.getStorepasswd(), refund.getTrno(),
				refund.getAuthty());

		return refundLogRepo.save(log);
	};

	/**
	 * 취소로그 상태값 수정
	 * - 환불 완료된 경우 해당 주문번호의 환불오류건 업데이트 
	 * @param trNo
	 * @param status
	 */
	public void updateRefundLogStatus(String trNo, String status) {
		
		List<KsnetRefundLog> logs = refundLogRepo.findByTrNoAndStatusCodeNot(trNo, KsnetRefundLog.STATUS_COMPLETED);
		
		logs.forEach(log -> {
			log.setStatusCode(KsnetRefundLog.STATUS_FINISH);
			refundLogRepo.save(log);
		});
	}
	
	/**
	 * 결제 취소 요청건 결과 저장
	 * 
	 * @param log
	 * @param result
	 */
	public void updateRefundLog(KsnetRefundLog log, KspayRefundReturns result) {

		log.setResult(result);
		refundLogRepo.save(log);
	}

	/**
	 * 기취소건인지 확인
	 * @param cancel
	 * @return
	 */
	public boolean hasCancelSuccessLog(KsnetRefundBody cancel) {

		if (refundLogRepo.findByTrNoAndRStatus(cancel.getTrno(), KSPAY_CANCEL_RSTATUS_TRUE).isPresent()) {
			return true;
		}
		
		return false;
	}

	/**
	 * 주문번호로 결제금액 찾기
	 * 
	 * @param trNo
	 * @return
	 */
	public KsnetPayResult getPaidResult(String trNo) {
		
		if (Sunpay.isEmpty(trNo)) {
			log.info("-- getPaidResult: null passed..");
			return null;
		} 

		Optional<KsnetPayResult> oPayResult = ksnetPayResultRepo.findByTrnoAndAuthyn(trNo, "O");
		
		return oPayResult.orElse(null);
	}

	public KspayRefundReturns sendKSPay(KsnetRefundBody cancel) {

		KspayRefundReturns returns = new KspayRefundReturns("", "X", "", "", "취소거절", "지원X");

		switch (cancel.getAuthty()) {
		// 신용카드 결제 취소
		case KSPAY_AUTHTY_CREDIT:
			returns = kspayCancelPostCredit(cancel);
			break;

		// 계좌이체 결제 취소(결제 당일)
		case KSPAY_AUTHTY_BANK_CANCEL:
			returns = kspayCancelPostBank(cancel);
			break;

		// 계좌이체 결제 환불
		case KSPAY_AUTHTY_BANK_REFUND:
			break;

		// 모바일 결제 취소
		case KSPAY_AUTHTY_MOBILE:
			returns = kspayCancelPostMobile(cancel);
			break;

		default:
			returns.setRTransactionNo("");
			returns.setRStatus("X");
			returns.setRTradeDate("");
			returns.setRTradeTime("");
			returns.setRMessage1("취소거절");
			returns.setRMessage2("승인구분 없음");
			break;
		}

		return returns;
	}

	public KspayRefundReturns kspayCancelPostBank(KsnetRefundBody cancel) {
		// Header부 Data --------------------------------------------------
		String EncType = "2"; // 0: 암화안함, 1:ssl, 2: seed
		String Version = "0603"; // 전문버전
		String Type = "00"; // 구분
		String Resend = "0"; // 전송구분 : 0 : 처음, 2: 재전송
		String RequestDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()); // 요청일자 :
		String KeyInType = "K"; // KeyInType 여부 : S : Swap, K: KeyInType
		String LineType = "1"; // lineType 0 : offline, 1:internet, 2:Mobile
		String ApprovalCount = "1"; // 복합승인갯수
		String GoodType = "0"; // 제품구분 0 : 실물, 1 : 디지털
		String HeadFiller = ""; // 예비

		// Header (입력값 (*) 필수항목)--------------------------------------------------
		String StoreId = cancel.getStoreid(); // *상점아이디
		String OrderNumber = ""; // 주문번호
		String UserName = ""; // 주문자명
		String IdNum = ""; // 주민번호 or 사업자번호
		String Email = ""; // email
		String GoodName = ""; // 제품명
		String PhoneNo = ""; // 휴대폰번호
		// Header end
		// -------------------------------------------------------------------

		// Data
		// Default------------------------------------------------------------------
		String ApprovalType = cancel.getAuthty();	// 승인구분 코드
		String TrNo = cancel.getTrno();	// 거래번호

		// Server로 부터 응답이 없을시 자체응답
		String rApprovalType = "2011"; // 승인구분
		String rACTransactionNo = TrNo; // 거래번호
		String rACStatus = "X"; // 오류구분 :승인 X:거절
		String rACTradeDate = RequestDate.substring(0, 8); // 거래 개시 일자(YYYYMMDD)
		String rACTradeTime = RequestDate.substring(8, 14); // 거래 개시 시간(HHMMSS)
		String rACAcctSele = ""; // 계좌이체 구분 - 1:Dacom, 2:Pop Banking, 3:실시간계좌이체 4: 승인형계좌이체
		String rACFeeSele = ""; // 선/후불제구분 - 1:선불, 2:후불
		String rACInjaName = ""; // 인자명(통장인쇄메세지-상점명)
		String rACPareBankCode = ""; // 입금모계좌코드
		String rACPareAcctNo = ""; // 입금모계좌번호
		String rACCustBankCode = ""; // 출금모계좌코드
		String rACCustAcctNo = ""; // 출금모계좌번호
		String rACAmount = ""; // 금액 (결제대상금액)
		String rACBankTransactionNo = ""; // 은행거래번호
		String rACIpgumNm = ""; // 입금자명
		String rACBankFee = "0"; // 계좌이체 수수료
		String rACBankAmount = ""; // 총결제금액(결제대상금액+ 수수료
		String rACBankRespCode = "9999"; // 오류코드
		String rACMessage1 = "취소거절"; // 오류 message 1
		String rACMessage2 = "C잠시후재시도"; // 오류 message 2
		String rACFiller = ""; // 예비
		
		KspayRefundReturns returns = new KspayRefundReturns(rACTransactionNo, rACStatus, rACTradeDate, rACTradeTime,
				rACMessage1, rACMessage2);

		try {
			KSPayApprovalCancelBean ipg = new KSPayApprovalCancelBean(IPG_IP_ADDR, IPG_PORT);

			ipg.HeadMessage(EncType, Version, Type, Resend, RequestDate, StoreId, OrderNumber, UserName, IdNum, Email,
					GoodType, GoodName, KeyInType, LineType, PhoneNo, ApprovalCount, HeadFiller);

			ipg.CancelDataMessage(ApprovalType, "0", TrNo, "", "", "", "", "");

			if (ipg.SendSocket("1")) {
				rApprovalType = ipg.ApprovalType[0];
				rACTransactionNo = ipg.ACTransactionNo[0]; // 거래번호
				rACStatus = ipg.ACStatus[0]; // 오류구분 :승인 X:거절
				rACTradeDate = ipg.ACTradeDate[0]; // 거래 개시 일자(YYYYMMDD)
				rACTradeTime = ipg.ACTradeTime[0]; // 거래 개시 시간(HHMMSS)
				rACAcctSele = ipg.ACAcctSele[0]; // 계좌이체 구분 - 1:Dacom, 2:Pop Banking, 3:Scrapping 계좌이체, 4:승인형계좌이체,
													// 5:금결원계좌이체
				rACFeeSele = ipg.ACFeeSele[0]; // 선/후불제구분 - 1:선불, 2:후불
				rACInjaName = ipg.ACInjaName[0]; // 인자명(통장인쇄메세지-상점명)
				rACPareBankCode = ipg.ACPareBankCode[0]; // 입금모계좌코드
				rACPareAcctNo = ipg.ACPareAcctNo[0]; // 입금모계좌번호
				rACCustBankCode = ipg.ACCustBankCode[0]; // 출금모계좌코드
				rACCustAcctNo = ipg.ACCustAcctNo[0]; // 출금모계좌번호
				rACAmount = ipg.ACAmount[0]; // 금액 (결제대상금액)
				rACBankTransactionNo = ipg.ACBankTransactionNo[0]; // 은행거래번호
				rACIpgumNm = ipg.ACIpgumNm[0]; // 입금자명
				rACBankFee = ipg.ACBankFee[0]; // 계좌이체 수수료
				rACBankAmount = ipg.ACBankAmount[0]; // 총결제금액(결제대상금액+ 수수료
				rACBankRespCode = ipg.ACBankRespCode[0]; // 오류코드
				rACMessage1 = ipg.ACMessage1[0]; // 오류 message 1
				rACMessage2 = ipg.ACMessage2[0]; // 오류 message 2
				rACFiller = ipg.ACFiller[0]; // 예비
				
				returns.setRTransactionNo(rACTransactionNo);
				returns.setRStatus(rACStatus);
				returns.setRTradeDate(rACTradeDate);
				returns.setRTradeTime(rACTradeTime);
				returns.setRMessage1(rACMessage1);
				returns.setRMessage2(rACMessage2);
			}
		} catch (Exception e) {
			rACMessage2 = "P잠시후재시도(" + e.toString() + ")"; // 메시지2
			returns.setRMessage2(rACMessage2);
		} // end of catch

		return returns;
	}

	public KspayRefundReturns kspayCancelPostMobile(KsnetRefundBody cancel) {

		// Default(수정항목이 아님)-------------------------------------------------------
		String EncType = "2"; // 0: 암화안함, 1:openssl, 2: seed
		String Version = "0210"; // 전문버전
		String Type = "00"; // 구분
		String Resend = "0"; // 전송구분 : 0 : 처음, 2: 재전송
		String RequestDate = new SimpleDateFormat("yyyyMMddhhmmss").format(new java.util.Date()); // 요청일자 :
																									// yyyymmddhhmmss
		String KeyInType = "K"; // KeyInType 여부 : S : Swap, K: KeyInType
		String LineType = "1"; // lineType 0 : offline, 1:internet, 2:Mobile
		String ApprovalCount = "1"; // 복합승인갯수
		String GoodType = "0"; // 제품구분 0 : 실물, 1 : 디지털
		String HeadFiller = ""; // 예비
		// -------------------------------------------------------------------------------

		// Header (입력값 (*) 필수항목)--------------------------------------------------
		String StoreId = cancel.getStoreid(); // *상점아이디
		String OrderNumber = ""; // 주문번호
		String UserName = ""; // 주문자명
		String IdNum = ""; // 주민번호 or 사업자번호
		String Email = ""; // email
		String GoodName = ""; // 제품명
		String PhoneNo = ""; // 휴대폰번호
		// Header end
		// -------------------------------------------------------------------

		// Data Default(수정항목이 아님)-------------------------------------------------
		String ApprovalType = cancel.getAuthty(); // 승인구분
		String TrNo = cancel.getTrno(); // 거래번호
		// Data Default end
		// -------------------------------------------------------------

		// 승인거절 응답
		// Server로 부터 응답이 없을시 자체응답
		String rApprovalType = "M111";
		String rTransactionNo = ""; // 거래번호
		String rStatus = "X"; // 상태 O : 승인, X : 거절
		String rTradeDate = ""; // 거래일자
		String rTradeTime = ""; // 거래시간
		String rBalAmount = ""; // 잔액
		String rRespCode = "PM09"; // 응답코드
		String rRespMsg = "C잠시후 재시도"; // 응답메시지
		String rBypassMsg = ""; // Echo항목
		String rCompCode = ""; // 서비스업체구분
		String rFiller = "";

		KspayRefundReturns returns = new KspayRefundReturns(rTransactionNo, rStatus, rTradeDate, rTradeTime, rRespCode,
				rRespMsg);

		try {
			KSPayApprovalCancelBean ipg = new KSPayApprovalCancelBean(IPG_IP_ADDR, IPG_PORT);

			ipg.HeadMessage(EncType, Version, Type, Resend, RequestDate, StoreId, OrderNumber, UserName, IdNum, Email,
					GoodType, GoodName, KeyInType, LineType, PhoneNo, ApprovalCount, HeadFiller);

			ipg.CancelDataMessage(ApprovalType, "0", TrNo, "", "", "", "", "");

			if (ipg.SendSocket("1")) {
				rApprovalType = ipg.ApprovalType[0]; // 승인구분 코드
				rTransactionNo = ipg.MTransactionNo[0]; // 거래번호
				rStatus = ipg.MStatus[0]; // 거래성공여부
				rTradeDate = ipg.MTradeDate[0]; // 거래일자
				rTradeTime = ipg.MTradeTime[0]; // 거래시간
				rBalAmount = ipg.MBalAmount[0]; // 잔액
				rRespCode = ipg.MRespCode[0]; // 응답코드
				rRespMsg = ipg.MRespMsg[0]; // 응답메시지
				rBypassMsg = ipg.MBypassMsg[0]; // Echo 필드
				rCompCode = ipg.MCompCode[0]; // 기관코드
				rFiller = ipg.MFiller[0]; // 예비

				returns.setRTransactionNo(rTransactionNo);
				returns.setRStatus(rStatus);
				returns.setRTradeDate(rTradeDate);
				returns.setRTradeTime(rTradeTime);
				returns.setRMessage1(rRespCode);
				returns.setRMessage2(rRespMsg);
			}

			if (rStatus == null || rStatus.substring(0, 1).equals("X")) // 취소거절의 경우 한번 더 전송한다.
			{
				if (ipg.SendSocket("1")) {
					rApprovalType = ipg.ApprovalType[0]; // 승인구분 코드
					rTransactionNo = ipg.MTransactionNo[0]; // 거래번호
					rStatus = ipg.MStatus[0]; // 거래성공여부
					rTradeDate = ipg.MTradeDate[0]; // 거래일자
					rTradeTime = ipg.MTradeTime[0]; // 거래시간
					rBalAmount = ipg.MBalAmount[0]; // 잔액
					rRespCode = ipg.MRespCode[0]; // 응답코드
					rRespMsg = ipg.MRespMsg[0]; // 응답메시지
					rBypassMsg = ipg.MBypassMsg[0]; // Echo 필드
					rCompCode = ipg.MCompCode[0]; // 기관코드
					rFiller = ipg.MFiller[0]; // 예비

					returns.setRTransactionNo(rTransactionNo);
					returns.setRStatus(rStatus);
					returns.setRTradeDate(rTradeDate);
					returns.setRTradeTime(rTradeTime);
					returns.setRMessage1(rRespCode);
					returns.setRMessage2(rRespMsg);
				}
			}
		} catch (Exception e) {
			rStatus = "X";
			rRespCode = "9999"; // 응답코드
			rRespMsg = "C취소거절"; // 응답메시지

			returns.setRStatus(rStatus);
			returns.setRMessage1(rRespCode);
			returns.setRMessage2(rRespMsg);
		} // end of catch

		return returns;
	}

	public KspayRefundReturns kspayCancelPostCredit(KsnetRefundBody cancel) {
		// Default(수정항목이 아님)-------------------------------------------------------
		String EncType = "0"; // 0: 암화안함, 1:openssl, 2: seed
		String Version = "0210"; // 전문버전
		String Type = "00"; // 구분
		String Resend = "0"; // 전송구분 : 0 : 처음, 2: 재전송
		String RequestDate = new SimpleDateFormat("yyyyMMddhhmmss").format(new java.util.Date()); // 요청일자 :
																									// yyyymmddhhmmss
		String KeyInType = "K"; // KeyInType 여부 : S : Swap, K: KeyInType
		String LineType = "1"; // lineType 0 : offline, 1:internet, 2:Mobile
		String ApprovalCount = "1"; // 복합승인갯수
		String GoodType = "0"; // 제품구분 0 : 실물, 1 : 디지털
		String HeadFiller = ""; // 예비
		// -------------------------------------------------------------------------------

		// Header (입력값 (*) 필수항목)--------------------------------------------------
		String StoreId = cancel.getStoreid(); // *상점아이디
		String OrderNumber = ""; // 주문번호
		String UserName = ""; // *주문자명
		String IdNum = ""; // 주민번호 or 사업자번호
		String Email = ""; // *email
		String GoodName = ""; // *제품명
		String PhoneNo = ""; // *휴대폰번호
		// Header end
		// -------------------------------------------------------------------

		// Data Default(수정항목이 아님)-------------------------------------------------
		String ApprovalType = cancel.getAuthty(); // 승인구분
		String TrNo = cancel.getTrno(); // 거래번호
		// Data Default end
		// -------------------------------------------------------------

		// Server로 부터 응답이 없을시 자체응답
		String rApprovalType = "1011";
		String rTransactionNo = ""; // 거래번호
		String rStatus = "X"; // 상태 O : 승인, X : 거절
		String rTradeDate = ""; // 거래일자
		String rTradeTime = ""; // 거래시간
		String rIssCode = "00"; // 발급사코드
		String rAquCode = "00"; // 매입사코드
		String rAuthNo = "9999"; // 승인번호 or 거절시 오류코드
		String rMessage1 = "취소거절"; // 메시지1
		String rMessage2 = "C잠시후재시도"; // 메시지2
		String rCardNo = ""; // 카드번호
		String rExpDate = ""; // 유효기간
		String rInstallment = ""; // 할부
		String rAmount = ""; // 금액
		String rMerchantNo = ""; // 가맹점번호
		String rAuthSendType = "N"; // 전송구분
		String rApprovalSendType = "N"; // 전송구분(0 : 거절, 1 : 승인, 2: 원카드)
		String rPoint1 = "000000000000"; // Point1
		String rPoint2 = "000000000000"; // Point2
		String rPoint3 = "000000000000"; // Point3
		String rPoint4 = "000000000000"; // Point4
		String rVanTransactionNo = "";
		String rFiller = ""; // 예비
		String rAuthType = ""; // ISP : ISP거래, MP1, MP2 : MPI거래, SPACE : 일반거래
		String rMPIPositionType = ""; // K : KSNET, R : Remote, C : 제3기관, SPACE : 일반거래
		String rMPIReUseType = ""; // Y : 재사용, N : 재사용아님
		String rEncData = ""; // MPI, ISP 데이터

		KspayRefundReturns returns = new KspayRefundReturns(rTransactionNo, rStatus, rTradeDate, rTradeTime, rMessage1,
				rMessage2);

		try {
			KSPayApprovalCancelBean ipg = new KSPayApprovalCancelBean(IPG_IP_ADDR, IPG_PORT);

			ipg.HeadMessage(EncType, Version, Type, Resend, RequestDate, StoreId, OrderNumber, UserName, IdNum, Email,
					GoodType, GoodName, KeyInType, LineType, PhoneNo, ApprovalCount, HeadFiller);

			ipg.CancelDataMessage(ApprovalType, "0", TrNo, "", "", "", "", "");

			if (ipg.SendSocket("1")) {
				rApprovalType = ipg.ApprovalType[0];
				rTransactionNo = ipg.TransactionNo[0]; // 거래번호
				rStatus = ipg.Status[0]; // 상태 O : 승인, X : 거절
				rTradeDate = ipg.TradeDate[0]; // 거래일자
				rTradeTime = ipg.TradeTime[0]; // 거래시간
				rIssCode = ipg.IssCode[0]; // 발급사코드
				rAquCode = ipg.AquCode[0]; // 매입사코드
				rAuthNo = ipg.AuthNo[0]; // 승인번호 or 거절시 오류코드
				rMessage1 = ipg.Message1[0]; // 메시지1
				rMessage2 = ipg.Message2[0]; // 메시지2
				rCardNo = ipg.CardNo[0]; // 카드번호
				rExpDate = ipg.ExpDate[0]; // 유효기간
				rInstallment = ipg.Installment[0]; // 할부
				rAmount = ipg.Amount[0]; // 금액
				rMerchantNo = ipg.MerchantNo[0]; // 가맹점번호
				rAuthSendType = ipg.AuthSendType[0]; // 전송구분= new String(this.read(2));
				rApprovalSendType = ipg.ApprovalSendType[0]; // 전송구분(0 : 거절, 1 : 승인, 2: 원카드)
				rPoint1 = ipg.Point1[0]; // Point1
				rPoint2 = ipg.Point2[0]; // Point2
				rPoint3 = ipg.Point3[0]; // Point3
				rPoint4 = ipg.Point4[0]; // Point4
				rVanTransactionNo = ipg.VanTransactionNo[0]; // Van거래번호
				rFiller = ipg.Filler[0]; // 예비
				rAuthType = ipg.AuthType[0]; // ISP : ISP거래, MP1, MP2 : MPI거래, SPACE : 일반거래
				rMPIPositionType = ipg.MPIPositionType[0]; // K : KSNET, R : Remote, C : 제3기관, SPACE : 일반거래
				rMPIReUseType = ipg.MPIReUseType[0]; // Y : 재사용, N : 재사용아님
				rEncData = ipg.EncData[0]; // MPI, ISP 데이터

				returns.setRTransactionNo(rTransactionNo);
				returns.setRStatus(rStatus);
				returns.setRTradeDate(rTradeDate);
				returns.setRTradeTime(rTradeTime);
				returns.setRMessage1(rMessage1);
				returns.setRMessage2(rMessage2);
				
			}
		} catch (Exception e) {
			rMessage2 = "P잠시후재시도(" + e.toString() + ")"; // 메시지2

			returns.setRMessage2(rMessage2);
		} // end of catch

		return returns;
	}
	
	public KspayRefundReturns refund(KsnetRefundBody refund) {
		// Default 결과값 생성
		KspayRefundReturns result = new KspayRefundReturns(refund.getTrno(), "X", "", "", "취소거절", "");
		
		// 결제 취소 요청 DB 저장
		refund.setStatusCode(KsnetRefundLog.STATUS_TRY);
		KsnetRefundLog refundLog = saveRefundLog(refund);
		
		// 주문정보 조회 
		KsnetPayResult paidResult = getPaidResult(refund.getTrno());
		if (paidResult == null) {
			refundLog.setStatusCode(KsnetRefundLog.STATUS_ERROR);
			result.setRMessage2("주문정보 없음");
			updateRefundLog(refundLog, result);
			return result;
		}
		
		refundLog.setKsnetPayResult(paidResult);
		
		// 결제건의 정산타입 저장
		refundLog.setServiceTypeCode(paidResult.getServiceTypeCd());
		
		// 결제건의 결제방법 저장
		refundLog.setPaymethodCode(paidResult.getKsnetPay().getSndPaymethod());
		
		// 기취소건 확인
		if (hasCancelSuccessLog(refund)) {
			refundLog.setStatusCode(KsnetRefundLog.STATUS_FINISH);
			result.setRMessage2("기취소거래건");
			updateRefundLog(refundLog, result);
			return result;
		}

		// 순간정산으로 결제했는지 확인
		boolean isInstantPaid = paidResult.getServiceTypeCd().equals("INSTANT") ? true : false;
		refundLog.setAmt(paidResult.getAmt());

		// (순간결제 + 카드결제)건의 취소요청 시 예치금 확인 및 차감
		if (isInstantPaid && refund.getAuthty().equals(KsnetService.KSPAY_AUTHTY_CREDIT)) {
			try {
				depositService.tryRefund(refund.getStoreid(), paidResult);
			} catch (DepositException ex) {
				
				// 예치금 부족시 PUSH알림
				if (ex.getErrCode() == DepositException.CODE_DEPOSIT_LACK) {
					depositService.pushRefundCancel(refund.getStoreid(), paidResult);
					
					// 순간정산 > 일반정산 전환 & 전환 알림
					try {
						boolean sendPush = true;
						storeService.instantOff(storeService.getStoreByStoreId(refund.getStoreid()), sendPush);
					} catch (Exception e) {
						log.log(Level.WARNING, "[환불요청]순간정산 > 일반정산 전환 실패(trNo: " + refund.getTrno() + ")");
					}
				}
				
				// 환불 중단
				refundLog.setStatusCode(KsnetRefundLog.STATUS_ERROR);
				result.setRMessage2(ex.getMessage());
				updateRefundLog(refundLog, result);
				return result;
				
			} catch (Exception ex) {
				
				// 환불 중단
				refundLog.setStatusCode(KsnetRefundLog.STATUS_ERROR);
				result.setRMessage2(ex.getMessage());
				updateRefundLog(refundLog, result);
				return result;
				
			}
		} 

		// KSPay 통신 시작
		result = sendKSPay(refund);
		// KSPay 통신 오류 시
		if (!result.getRStatus().equals("O")) {
			
			// 순간정산이었다면 예치금원복
			if (isInstantPaid) {
				try {
					depositService.resetDeposit(refund);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.log(Level.WARNING, "DepositError: trNo - " + refund.getTrno());
				}
			}
			
			refundLog.setStatusCode(KsnetRefundLog.STATUS_ERROR);
			updateRefundLog(refundLog, result);
			return result;
		}
		
		// KSPay 통신 성공, 환불 완료 처리
		// 순간정산 환불 시 처리
		if (isInstantPaid) {
			try {
				depositService.completeRefund(refund);
			} catch (Exception e) {
				log.log(Level.WARNING, "DepositError: trNo - " + refund.getTrno());
			}
		}
		
		refundLog.setStatusCode(KsnetRefundLog.STATUS_COMPLETED);
		
		// 환불 완료 시 기존 환불 시도/실패건들 모두 완료상태로 업데이트
		updateRefundLogStatus(refundLog.getTrNo(), KsnetRefundLog.STATUS_FINISH);
		updateRefundLog(refundLog, result);
		
		// 취소 결과 PUSH 발송
		pushService.sendPush(refundLog);
		
		return result;
	}
}
