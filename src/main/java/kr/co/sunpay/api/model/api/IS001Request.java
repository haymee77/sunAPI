package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.ApiRequestBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IS001Request extends ApiRequestBody {

	// 조회ID
	@ApiModelProperty(notes = "조회하고자 하는 ID")
	private String searchId;

}
