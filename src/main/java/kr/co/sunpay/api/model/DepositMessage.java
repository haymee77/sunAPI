package kr.co.sunpay.api.model;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description="예치금 입금 문자 전송 모델")
public class DepositMessage {

	private String key;

	private String message;
}
