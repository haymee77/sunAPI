package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.ApiResponseBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IS002Response extends ApiResponseBody {
	
	// 회원ID
	@ApiModelProperty(notes="요청한 회원ID")
	private String memberId;
	
}
