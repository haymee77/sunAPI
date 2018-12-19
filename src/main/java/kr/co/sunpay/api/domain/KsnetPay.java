package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(of="uid")
@ToString
public class KsnetPay {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@Column(name="PAYMETHOD", length=20)
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
	
	@CreationTimestamp
	@Column(name="CREATED_DT")
	private LocalDateTime createdDate;
	
	@UpdateTimestamp
	@Column(name="UPDATED_DT")
	private LocalDateTime updatedDate;
}