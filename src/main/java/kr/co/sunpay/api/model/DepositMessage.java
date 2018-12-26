package kr.co.sunpay.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepositMessage {

	private String key;

	private String message;
}
