package kr.co.sunpay.api.model;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Store;
import kr.co.sunpay.api.domain.StoreId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreRequest {

	// 사업자 정보
	@ApiModelProperty(notes = "사업자 구분(CODES.GROUP_NM='BIZ_TYPE')")
	@NotBlank(message = "사업자 구분을 선택해주세요.")
	private String bizTypeCode;

	@ApiModelProperty(notes = "사업자 등록번호")
	@Size(max = 15, message = "사업자 등록번호는 12자까지 입력 가능합니다.")
	@Pattern(regexp = "[0-9]{12}", message = "숫자만 입력가능합니다.")
	private String bizNo;

	@ApiModelProperty(notes = "상점명(사업자 상호명)")
	@Size(max = 50, message = "사업자 상호명은 50자까지 입력 가능합니다.")
	private String bizName;

	@ApiModelProperty(notes = "사업자 성명")
	@Size(max = 8, message = "사업자 성명은 8자까지 입력 가능합니다.")
	private String bizOwner;

	@ApiModelProperty(notes = "사업자 주민번호")
	@Size(max = 15)
	private String bizOwnerRegiNo;

	@ApiModelProperty(notes = "사업자 우편번호")
	@Size(max = 10)
	private String bizZipcode;

	@ApiModelProperty(notes = "사업자 주소-기본")
	@Size(max = 100)
	private String bizAddressBasic;

	@ApiModelProperty(notes = "사업자 주소-상세")
	@Size(max = 100)
	private String bizAddressDetail;

	@ApiModelProperty(notes = "사업자 연락처")
	@Pattern(regexp = "[0-9]{9,11}", message = "9~11자리의 숫자만 입력가능합니다")
	private String bizContact;

	@NotBlank(message = "메일을 작성해주세요.")
	@Size(max = 100)
	@Email(message = "메일의 양식을 지켜주세요.")
	@ApiModelProperty(notes = "사업장 대표 메일")
	private String bizMail;

	@ApiModelProperty(notes = "업종")
	@Size(max = 200)
	private String bizIndustry;

	@ApiModelProperty(notes = "업태")
	@Size(max = 200)
	private String bizStatus;

	@ApiModelProperty(notes = "상점 URL")
	@Size(max = 200)
	private String storeUrl;

	@ApiModelProperty(notes = "보증보험 가입여부")
	private Boolean suretyInsurance = false;

	@ApiModelProperty(notes = "보증보험 가입액")
	private Integer suretyInsuranceAmt = 0;
	
	// 결제 관련 정보
	@ApiModelProperty(notes = "은행코드")
	private String bankCode;

	@ApiModelProperty(notes = "계좌번호")
	private String bankAccountNo;

	@ApiModelProperty(notes = "계좌주명")
	private String bankAccountName;
	
	@ApiModelProperty(notes = "신용카드 결제 시 최대 할부 개월 수")
	private Integer maxInstallmentLimit;
	
	@ApiModelProperty(notes="최소결제금액")
	private Integer minPaymentAmt = 0;

	@ApiModelProperty(notes="1회 결제한도")
	private Integer paymentLimitOnce = 0;
	
	@ApiModelProperty(notes="1일 결제한도")
	private Integer paymentLimitDaily = 0;
	
	@ApiModelProperty(notes="월간 결제한도")
	private Integer paymentLimitMonthly = 0;
	
//	@ApiModelProperty(notes="분기 결제한도")
//	private Integer paymentLimitQuarterly = 0;
//	
//	@ApiModelProperty(notes="연간 결제한도")
//	private Integer paymentLimitAnnual = 0;	
	
	@ApiModelProperty(notes="PG수수료(%단위) - PG사")
	private Double feePg = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 본사")
	private Double feeHead = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 지사")
	private Double feeBranch = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 대리점")
	private Double feeAgency = 0.0;
	
	@ApiModelProperty(notes="건당 송금수수료 - PG")
	private Integer transFeePg = 0;
	
	@ApiModelProperty(notes="건당 송금수수료 - 본사")
	private Integer transFeeHead = 0;
	
	@ApiModelProperty(notes="건당 송금수수료 - 지사")
	private Integer transFeeBranch = 0;
	
	@ApiModelProperty(notes="건당 송금수수료 - 대리점")
	private Integer transFeeAgency = 0;
	
	// 기본 상점 정보
	@ApiModelProperty(notes = "최소 취소예치금")
	private int minDeposit;

	@ApiModelProperty(notes = "취소예치금 입금번호")
	private String depositNo;
	
	@ApiModelProperty(notes="가입비")
	private Integer membershipFee = 0;
	
	@ApiModelProperty(notes="소속 그룹의 UID")
	@NotBlank(message="소속 그룹의 정보가 누락되었습니다.")
	private int groupUid;

	// 담당자 정보
	@ApiModelProperty(notes = "담당자 가입정보")
	private MemberRequest memberReq;
	
	public Store toEntity(Group group) {
		return new Store(bizTypeCode, bizNo, bizName, bizOwner, bizOwnerRegiNo, bizZipcode, bizAddressBasic,
				bizAddressDetail, bizContact, bizMail, bizIndustry, bizStatus, storeUrl, suretyInsurance,
				suretyInsuranceAmt, bankCode, bankAccountNo, bankAccountName, maxInstallmentLimit, minPaymentAmt,
				paymentLimitOnce, paymentLimitDaily, paymentLimitMonthly, feePg, feeHead, feeBranch, feeAgency,
				transFeePg, transFeeHead, transFeeBranch, transFeeAgency, minDeposit, depositNo, membershipFee, group);
	}
}
