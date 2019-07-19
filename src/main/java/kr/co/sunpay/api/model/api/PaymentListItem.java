package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentListItem {

	@ApiModelProperty(notes="결제일(PG사 기준)")
	private String paidDate;
	
	@ApiModelProperty(value="결제금액(PG사 실제 결제금액)")
	private int amount = 0;
	
	@ApiModelProperty(notes="서비스 타입")
	private String serviceType;
	
	@ApiModelProperty(notes="PG사 결제번호(영수증번호)")
	private String trNo;
	
	@ApiModelProperty(notes="결제방법")
	private String paymethod;
	
	@ApiModelProperty(notes="상품명(쇼핑몰에서 넘어온 값)")
	private String goodsName;
	
	@ApiModelProperty(notes="주문자명")
	private String orderName;
	
	@ApiModelProperty(notes="상점 주문번호")
	private String orderNo;
	
}
