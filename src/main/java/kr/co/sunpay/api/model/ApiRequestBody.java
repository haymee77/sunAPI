package kr.co.sunpay.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiRequestBody {

	// SUN-KEY
	@ApiModelProperty(notes="썬페이에서 발급한 연동키")
	private String sunKey;

	// 지사ID
	@ApiModelProperty(notes="소속된 지사 ID")
	private String branchId;
	
	// 대리점ID
	@ApiModelProperty(notes="소속된 대리점 ID")
	private String agencyId;
	
	// 관리자ID
	@ApiModelProperty(notes="썬페이에 등록된 관리자 ID")
	private String adminId;
}
