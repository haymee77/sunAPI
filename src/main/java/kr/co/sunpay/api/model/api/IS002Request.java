package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.ApiRequestBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IS002Request extends ApiRequestBody {

	// 회원ID
	@ApiModelProperty(notes = "생성하고자 하는 ID")
	private String memberId;
	
	// 비밀번호
	@ApiModelProperty(notes = "비밀번호")
	private String memberPassword;
	
	// 사업장명
	@ApiModelProperty(notes = "사업장명")
	private String bizNm;
	
	// 사업자 종류
	@ApiModelProperty(notes = "사업자 종류")
	private String bizTypeCd;
	
	// 결제(PG)수수료 - 대리점(%단위)
	@ApiModelProperty(notes = "결제(PG)수수료")
	private double fee;
	
	// 순간정산 건당 송금수수료 - 대리점
	@ApiModelProperty(notes = "순간정산 건당 송금수수료")
	private int transFee;

}
