package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.ApiRequestBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IS006Request extends ApiRequestBody {

	@ApiModelProperty(notes = "회원ID")
	private String memberId;
	
	// 정산 요청 월
	@ApiModelProperty(notes="정산 내역을 요청하는 월")
	private String settleMonth;

}
