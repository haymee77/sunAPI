package kr.co.sunpay.api.model;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.Store;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreRequest {

	@NotNull
	@ApiModelProperty(notes="소속 그룹 UID")
	private int groupUid;
	
	// 상점 정보
	@ApiModelProperty(notes="사업자 성명")
	@NotBlank(message="대표자 이름을 입력해주세요.")
	private String bizOwner;
	
	@ApiModelProperty(notes="사업자 주민번호")
	private String bizOwnerRegiNo;
	
	@ApiModelProperty(notes="사업자 등록번호")
	@Pattern(regexp = "[0-9]{10}", message = "사업자 등록번호 형식이 맞지 않습니다.")
	private String bizNo;
	
	@NotBlank(message="사업자 구분을 선택해주세요.")
	private String bizTypeCode;
	
	@ApiModelProperty(notes="사업자 상호명")
	@NotBlank(message="상점명을 입력해주세요.")
	private String bizName;
	
	@ApiModelProperty(notes="사업자 우편번호")
	@NotBlank(message="사업장 우편번호를 입력해주세요.")
	private String bizZipcode;
	
	@ApiModelProperty(notes="사업자 주소-기본")
	@NotBlank(message="사업장 주소를 입력해주세요.")
	private String bizAddressBasic;
	
	@ApiModelProperty(notes="사업자 주소-상세")
	@NotBlank(message="사업장 상세 주소를 입력해주세요.")
	private String bizAddressDetail;
	
	@NotBlank(message="사업장 이메일을 입력해주세요.")
	@Email(message="사업장 이메일이 메일 형식에 맞지 않습니다.")
	private String bizMail;

	@ApiModelProperty(notes="사업자 연락처")
	@NotBlank(message="사업장 연락처를 입력해주세요.")
	@Pattern(regexp = "[0-9]{8,12}", message = "8~12자리의 숫자만 입력가능합니다")
	private String bizContact;
	
	@ApiModelProperty(notes="업태")
	private String bizStatus;
	
	@ApiModelProperty(notes="업종")
	private String bizIndustry;
	
	@ApiModelProperty(notes="상점 URL")
	@NotBlank(message="쇼핑몰 웹주소를 입력해주세요.")
	@Pattern(regexp="^(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{2,3}.?([a-z]+)?$", message="쇼핑몰 웹주소의 URL 형식을 확인해주세요.")
	private String storeUrl;
	
	@ApiModelProperty(notes="보증보험 가입여부")
	private Boolean suretyInsurance = false;
	
	@ApiModelProperty(notes="보증보험 가입액")
	private Integer suretyInsuranceAmt = 0;
	
	// 예치금 관련 정보
	@ApiModelProperty(notes="취소예치금 입금번호")
	private String depositNo;
	
	@ApiModelProperty(notes="최소취소예치금")
	private int minDeposit;
	
	// 결제 관련 정보
	@ApiModelProperty(notes="신용카드 결제 시 최대 할부 개월 수")
	private int maxInstallmentLimit;
	
	@ApiModelProperty(notes="최소결제금액")
	private Integer minPaymentAmt = 0;
	
	@ApiModelProperty(notes="은행코드")
	private String bankCode;
	
	@ApiModelProperty(notes="계좌번호")
	private String bankAccountNo;
	
	@ApiModelProperty(notes="계좌주명")
	private String bankAccountName;
	
	@ApiModelProperty(notes="1회 결제한도")
	private Integer paymentLimitOnce = 0;
	
	@ApiModelProperty(notes="1일 결제한도")
	private Integer paymentLimitDaily = 0;
	
	@ApiModelProperty(notes="월간 결제한도")
	private Integer paymentLimitMonthly = 0;
	
	@ApiModelProperty(notes="분기 결제한도")
	private Integer paymentLimitQuarterly = 0;
	
	@ApiModelProperty(notes="연간 결제한도")
	private Integer paymentLimitAnnual = 0;
	
	// 수수료 정보 - PG 수수료
	@ApiModelProperty(notes="PG수수료(%단위) - PG사")
	private Double feePg = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 본사")
	private Double feeHead = 0.0;

	@ApiModelProperty(notes="PG수수료(%단위) - 지사")
	private Double feeBranch = 0.0;

	@ApiModelProperty(notes="PG수수료(%단위) - 대리점")
	private Double feeAgency = 0.0;
	
	// 수수료 정보 - 송금수수료
	@ApiModelProperty(notes="건당 송금수수료 - PG")
	private Integer transFeePg = 0;

	@ApiModelProperty(notes="건당 송금수수료 - 본사")
	private Integer transFeeHead = 0;

	@ApiModelProperty(notes="건당 송금수수료 - 지사")
	private Integer transFeeBranch = 0;

	@ApiModelProperty(notes="건당 송금수수료 - 대리점")
	private Integer transFeeAgency = 0;
	
	// 가입비
	@ApiModelProperty(notes="가입비")
	private Integer membershipFee = 0;
	
	@Valid
	private MemberRequest memberReq;
	
	public Store toEntity() {
		return new Store(bizOwner, bizOwnerRegiNo, bizNo, bizTypeCode, bizName, bizZipcode, bizAddressBasic,
				bizAddressDetail, bizMail, bizContact, bizStatus, bizIndustry, storeUrl, suretyInsurance,
				suretyInsuranceAmt, depositNo, minDeposit, maxInstallmentLimit, minPaymentAmt, bankCode, bankAccountNo,
				bankAccountName, paymentLimitOnce, paymentLimitDaily, paymentLimitMonthly, paymentLimitQuarterly,
				paymentLimitAnnual, feePg, feeHead, feeBranch, feeAgency, transFeePg, transFeeHead, transFeeBranch,
				transFeeAgency, membershipFee);
	}
}
