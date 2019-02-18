package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@Table(name="SP_KSNET_PAY_RESULT")
@EqualsAndHashCode(of="uid")
@ToString
public class KsnetPayResult {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	// 거래 상점 ID
	@Column(name="STORE_ID", length=20)
	private String storeId;
	
	// 정산서비스 타입(순간정산, D+2정산...)
	@Column(name="SERVICE_TYPE_CD", length=20)
	private String serviceTypeCd;
	
	// 성공여부: 영어대문자 O-성공, X-거절
	@Column(name="AUTH_YN", length=1)
	private String authyn;
	
	// 거래번호
	@Column(name="TR_NO", length=20)
	private String trno;
	
	// 거래일자(YYYYMMDD)
	@Column(name="TRD_DT", length=20)
	private String trddt;
	
	// 거래시간(HHMMTT)
	@Column(name="TRD_TM", length=20)
	private String trdtm;
	
	// 금액
	@Column(name="AMT")
	private int amt;
	
	// 메세지1
	@Column(name="MSG1", length=30)
	private String msg1;
	
	// 메세지2
	@Column(name="MSG2", length=30)
	private String msg2;
	
	// 주문번호
	@Column(name="ORD_NO", length=40)
	private String ordno;
	
	// 결제수단
	@Column(name="RESULT", length=10)
	private String result;
	
	// 응답코드
	@Column(name="RESULT_CD", length=20)
	private String resultcd;
	
	// 신용카드 결과항목 - 승인번호(결제 성공시에만)
	@Column(name="AUTH_NO", length=20)
	private String authno;
	
	// 신용카드 결과항목 - 발급사코드 || 가상계좌 결과항목 계좌번호 
	@Column(name="ISS_CD", length=20)
	private String isscd;
	
	// 신용카드 결과항목 - 매입사코드 || 휴대폰결제 결과항목 - 실물구분: 1- 실물/2-디지털 
	@Column(name="AQU_CD", length=20)
	private String aqucd;
	
	// 현금영수증번호
	@Column(name="CBTR_NO", length=50)
	private String cbtrno;
	
	// 할부개월수 
	@Column(name="INSTALMENT", length=3)
	private String halbu;
	
	@CreationTimestamp
	@Column(name="CREATED_DT")
	private LocalDateTime createdDate;
	
	@UpdateTimestamp
	@Column(name="UPDATED_DT")
	private LocalDateTime updatedDate;
	
	@OneToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="KSNET_PAY_UID_FK")
	private KsnetPay ksnetPay;
	
	public String msgGenerator() {
		String msg = "";
		
		msg = "상품명: " + this.ksnetPay.getSndGoodname() 
				+ "\n결제금액: " + getAmt();
		
		if (getAuthyn().equals("O")) {
			msg += "\n결제완료";
		} else {
			msg += "\n" + msg1 + "\n" + msg2;
		}
		
		return msg;
	}
}
