package kr.co.sunpay.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description="결제 취소 요청 시 Parameters")
public class KsnetRefundBody {

	@ApiModelProperty(notes="상점 ID", example="2999199999", required=true)
	private String storeid;
	
	private String storepasswd;
	
	@ApiModelProperty(notes="주문번호", example="169630089300", required=true)
	private String trno;
	
	@ApiModelProperty(notes="승인구분(신용카드:1010)", example="1010", required=true)
	private String authty;
}
