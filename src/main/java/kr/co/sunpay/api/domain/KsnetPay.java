package kr.co.sunpay.api.domain;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * KSNet으로 전달되는 결제데이터
 * @author himeepark
 *
 */
@Getter
@Setter
@Entity
@Table(name="SP_KSNET_PAY")
@Where(clause="DELETED<>1")
@SQLDelete(sql="UPDATE SP_KSNET_PAY SET DELETED=1 WHERE UID=?")
@ToString
public class KsnetPay extends BaseEntity {
	
	// 결제수단(신용카드, 계좌이체...)
	@Column(name="KSNET_PAYMETHOD_CD", length=20)
	private String sndPaymethod;
	
	@Column(name="STORE_ID", length=20)
	private String sndStoreid;
	
	@Column(name="ORDER_NO", length=40)
	private String sndOrdernumber;
	
	@Column(name="GOODS_NM", length=40)
	private String sndGoodname;
	
	@Column(name="AMOUNT")
	private int sndAmount;
	
	@Column(name="ORDER_NM", length=40)
	private String sndOrdername;
	
	@Column(name="EMAIL", length=40)
	private String sndEmail;
	
	@Column(name="MOBILE", length=20)
	private String sndMobile;
	
	@Column(name="SERVICE_PERIOD", length=40)
	private String sndServicePeriod;
	
	@Column(name="REPLY", length=200)
	private String sndReply;
	
}