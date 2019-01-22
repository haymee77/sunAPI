package kr.co.sunpay.api.model;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description="결제 취소 요청 시 Parameters")
public class KSPayCancelBody {

	private String storeid;
	
	private String storepasswd;
	
	private String trno;
	
	private String authty;
}
