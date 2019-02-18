package kr.co.sunpay.api.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="SP_TRADES")
public class Trade extends BaseEntity {

	@OneToOne
	@JoinColumn(name="KSNET_PAY_RESULT_UID")
	private KsnetPayResult ksnetPayResult;
}
