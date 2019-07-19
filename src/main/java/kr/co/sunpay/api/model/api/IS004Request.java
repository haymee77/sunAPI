package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.ApiRequestBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IS004Request extends ApiRequestBody {

	@ApiModelProperty(notes = "회원ID")
	private String memberId;
	
	// 정산
	@ApiModelProperty(notes="정산(일반정산/순간정산)")
	private String serviceType;

}
