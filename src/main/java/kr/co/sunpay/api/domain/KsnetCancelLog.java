package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import kr.co.sunpay.api.model.KspayCancelReturns;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_KSNET_CANCEL_LOGS")
@ToString
public class KsnetCancelLog {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@Column(name="CREATED_DT")
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name="STORE_ID")
	private String storeId;
	
	@Column(name="STORE_PASSWD")
	private String storePasswd;
	
	@Column(name="TR_NO")
	private String trNo;
	
	@Column(name="AMT")
	private int amt;

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
	
	public KsnetCancelLog(String storeId, String storePasswd, String trNo, String authty) {
		this.storeId = storeId;
		this.storePasswd = storePasswd;
		this.trNo = trNo;
		this.authty = authty;
	}

	public void setResult(KspayCancelReturns result) {
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
				+ "\n취소금액: " + getAmt();
		
		return msg;
	}
}
