package kr.co.sunpay.api.model.api;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.ApiResponseBody;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IS004Response extends ApiResponseBody {
	
	// 회원ID
	@ApiModelProperty(notes="요청한 회원ID")
	private String memberId;
	
	// 상태
	@ApiModelProperty(notes="상태(미가입/가입신청/가입완료)")
	private String status;
	
	// 정산
	@ApiModelProperty(notes="정산(일반정산/순간정산)")
	private String serviceType;
	
	// 예치금
	@ApiModelProperty(notes="예치금 잔액")
	private int deposit;
	
}
