package kr.co.sunpay.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_PAYMENT")
@ToString
public class Payment {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@Column(name="STORE_ID", length=15)
	private String storeId;
	
	@Column(name="ORDER_NO", length=25)
	private String orderNo;
	
	@Column(name="PAY_METHOD", length=15)
	private String payMethod;
	
	@Column(name="GOOD_NM", length=45)
	private String goodName;
	
	@Column(name="AMOUNT")
	private int amount;
}
