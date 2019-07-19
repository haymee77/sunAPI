package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.ApiRequestBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IS005Request extends ApiRequestBody {

	@ApiModelProperty(notes = "회원ID")
	private String memberId;
	
	// 결제 날짜
	@ApiModelProperty(notes="결제 날짜")
	private String paymentDate;

}
