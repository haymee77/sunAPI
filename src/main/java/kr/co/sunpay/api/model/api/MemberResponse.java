package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.ApiResponseBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponse extends ApiResponseBody {
	
	// 회원UID
	@ApiModelProperty(notes="요청한 회원UID")
	private int uid;
	
}
