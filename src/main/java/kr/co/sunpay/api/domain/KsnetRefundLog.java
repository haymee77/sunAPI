package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import kr.co.sunpay.api.model.KspayRefundReturns;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_KSNET_REFUND_LOGS")
@ToString
public class KsnetRefundLog {
	
	// 환불 상태 코드
	public static final String STATUS_COMPLETED = "COMPLETED";	// 오류 없이 환불완료
	public static final String STATUS_ERROR = "ERROR";			// 환불 오류
	public static final String STATUS_FINISH = "FINISH";		// 환불 오류였으나 처리됨(환불됨)
	public static final String STATUS_CANCEL = "CANCEL";		// 환불 오류건이었으나 취소됨
	public static final String STATUS_TRY = "TRY";				// 환불 요청 시도

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@Column(name="CREATED_DT")
	@CreationTimestamp
	private LocalDateTime createdDate;
	
	@Column(name="STATUS_CD")
	private String statusCode;

	@Column(name="STORE_ID")
	private String storeId;
	
	@Column(name="STORE_PASSWD")
	private String storePasswd;
	
	@Column(name="TR_NO")
	private String trNo;
	
	// 정산서비스 타입(순간정산, D+2정산...)
	@Column(name = "SERVICE_TYPE_CD", length = 20)
	private String serviceTypeCode;
	
	// 결제수단(신용카드, 계좌이체...)
	@Column(name = "KSNET_PAYMETHOD_CD", length = 20)
	private String paymethodCode;

	@Column(name="AMT")
	private Integer amt;

	@Column(name="AUTHTY")
	private String authty;
	
	@Column(name="R_TR_NO")
	private String rTrNo;
	
	@Column(name="R_STATUS")
	private String rStatus;
	
	@Column(name="R_TRADE_DATE")
	private String rTradeDate;
	
	@Column(name="R_TRADE_TIME")
	private String rTradeTime;
	
	@Column(name="R_MSG1")
	private String rMsg1;
	
	@Column(name="R_MSG2")
	private String rMsg2;
	
	@JsonBackReference(value="ksnetPayResult")
	@ManyToOne
	@JoinColumn(name="KSNET_PAY_RESULT_UID_FK")
	private KsnetPayResult ksnetPayResult;
	
	public KsnetRefundLog() {
	}
	
	public KsnetRefundLog(String storeId, String storePasswd, String trNo, String authty, String statusCode) {
		this.storeId = storeId;
		this.storePasswd = storePasswd;
		this.trNo = trNo;
		this.authty = authty;
		this.statusCode = statusCode;
	}

	public void setResult(KspayRefundReturns result) {
		this.rTradeDate = result.getRTradeDate();
		this.rTradeTime = result.getRTradeTime();
		this.rTrNo = result.getRTransactionNo();
		this.rStatus = result.getRStatus();
		this.rMsg1 = result.getRMessage1();
		this.rMsg2 = result.getRMessage2();
	}
	
	public String msgGenerator() {
		String msg = "[결제취소]"
				+ "\n주문번호: " + getTrNo()
				+ "\n정산타입: " + getServiceTypeCode()
				+ "\n환불금액: " + getAmt()
				+ "\n결과: " + getRMsg1() + "-" + getRMsg2();
		
		return msg;
	}
}
