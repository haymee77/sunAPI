package kr.co.sunpay.api.model;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import kr.co.sunpay.api.domain.Store;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreRequest {

	@NotNull
	private int groupUid;
	
	// 상점 정보
	@NotBlank
	private String bizOwner;
	
	private String bizOwnerRegiNo;
	
	private String bizNo;
	
	@NotBlank
	private String bizTypeCode;
	
	@NotBlank
	private String bizName;
	
	@NotBlank
	private String bizZipcode;
	
	@NotBlank
	private String bizAddressBasic;
	
	@NotBlank
	private String bizAddressDetail;
	
	@NotBlank
	private String bizMail;
	
	@NotBlank
	private String bizContact;
	
	private String bizStatus;
	
	private String bizIndustry;

	@NotBlank
	private String storeUrl;
	
	private Boolean suretyInsurance = false;
	
	private Integer suretyInsuranceAmt = 0;
	
	// 예치금 관련 정보
	private String depositNo;
	
	private int minDeposit;
	
	// 결제 관련 정보
	private int maxInstallmentLimit;
	
	private Integer minPaymentAmt = 0;
	
	private String bankCode;
	
	private String bankAccountNo;
	
	private String bankAccountName;
	
	private Integer paymentLimitOnce = 0;
	
	private Integer paymentLimitDaily = 0;
	
	private Integer paymentLimitMonthly = 0;
	
	private Integer paymentLimitQuarterly = 0;
	
	private Integer paymentLimitAnnual = 0;
	
	// 수수료 정보 - PG 수수료
	private Double feePg = 0.0;
	
	private Double feeHead = 0.0;
	
	private Double feeBranch = 0.0;
	
	private Double feeAgency = 0.0;
	
	// 수수료 정보 - 송금수수료
	private Integer transFeePg = 0;
	
	private Integer transFeeHead = 0;
	
	private Integer transFeeBranch = 0;
	
	private Integer transFeeAgency = 0;
	
	// 가입비
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
