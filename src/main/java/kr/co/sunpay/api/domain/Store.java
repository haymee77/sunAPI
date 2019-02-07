package kr.co.sunpay.api.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "SP_STORES")
@Where(clause = "DELETED<>1")
@SQLDelete(sql = "UPDATE SP_STORES SET DELETED=1 WHERE UID=?")
@ToString
public class Store extends BaseEntity {

	@ApiModelProperty(notes="사업자 종류(CODES.GROUP_NM='BIZ_TYPE')")
	@Column(name="BIZ_TYPE_CD", length=20)
	private String bizTypeCode;
	
	@ApiModelProperty(notes="은행코드")
	@Column(name="BANK_CD", length=20)
	private String bankCode;
	
	@ApiModelProperty(notes="계좌번호")
	@Column(name="BANK_ACCOUNT_NO", length=45)
	private String bankAccountNo;
	
	@ApiModelProperty(notes="계좌주명")
	@Column(name="BANK_ACCOUNT_NM", length=10)
	private String bankAccountName;
	
	@ApiModelProperty(notes="취소예치금 금액")
	@Column(name="DEPOSIT")
	private int deposit;
	
	@ApiModelProperty(notes="취소예치금 입금번호")
	@Column(name="DEPOSIT_NO", length=10)
	private String depositNo;
	
	@ApiModelProperty(notes="사업자 등록번호")
	@Column(name="BIZ_NO", length=15)
	private String bizNo;
	
	@ApiModelProperty(notes="사업자 상호명")
	@Column(name="BIZ_NM", length=50)
	private String bizName;
	
	@ApiModelProperty(notes="사업자 성명")
	@Column(name="BIZ_OWNER", length=10)
	private String bizOwner;
	
	@ApiModelProperty(notes="사업자 주민번호")
	@Column(name="BIZ_OWNER_REGI_NO", length=15)
	private String bizOwnerRegiNo;
	
	@ApiModelProperty(notes="사업자 우편번호")
	@Column(name="BIZ_ZIPCODE", length=10)
	private String bizZipcode;
	
	@ApiModelProperty(notes="사업자 주소-기본")
	@Column(name="BIZ_ADDRESS_BASIC", length=100)
	private String bizAddressBasic;
	
	@ApiModelProperty(notes="사업자 주소-상세")
	@Column(name="BIZ_ADDRESS_DETAIL", length=100)
	private String bizAddressDetail;
	
	@ApiModelProperty(notes="사업자 연락처")
	@Column(name="BIZ_CONTACT", length=25)
	private String bizContact;
	
	@ApiModelProperty(notes="업종")
	@Column(name="BIZ_INDUSTRY", length=200)
	private String bizIndustry;
	
	@ApiModelProperty(notes="업태")
	@Column(name="BIZ_STATUS", length=200)
	private String bizStatus;
	
	// TODO BIZ_EXTRA_INFO > 필요한 데이터인지 확인..영수증에 표기될 상호명..결제데이터 넘길 때 상호명 넘길 수 있음..
	
	@ApiModelProperty(notes="신용카드 결제 시 최대 할부 개월 수")
	@Column(name="MAX_INSTALLMENT_LIMIT")
	private Integer maxInstallmentLimit;
	
	@ApiModelProperty(notes="상점 URL")
	@Column(name="STORE_URL", length=200)
	private String storeUrl;
	
	@ApiModelProperty(notes="보증보험 가입여부")
	@Column(name="SURETY_INSURANCE_FL", columnDefinition="BIT(1) DEFAULT 0")
	private Boolean suretyInsurance = false;
	
	@ApiModelProperty(notes="보증보험 가입액")
	@Column(name="SURETY_INSURANCE_AMOUNT", columnDefinition="DEFAUNT 0")
	private Integer suretyInsuranceAmt = 0;
	
	@ApiModelProperty(notes="1회 결제한도")
	@Column(name="PAYMENT_LIMIT_ONCE")
	private Integer paymentLimitOnce = 0;
	
	@ApiModelProperty(notes="1일 결제한도")
	@Column(name="PAYMENT_LIMIT_DAILY")
	private Integer paymentLimitDaily = 0;
	
	@ApiModelProperty(notes="월간 결제한도")
	@Column(name="PAYMENT_LIMIT_MONTHLY")
	private Integer paymentLimitMonthly = 0;
	
	@ApiModelProperty(notes="분기 결제한도")
	@Column(name="PAYMENT_LIMIT_QUARTERLY")
	private Integer paymentLimitQuarterly = 0;
	
	@ApiModelProperty(notes="연간 결제한도")
	@Column(name="PAYMENT_LIMIT_ANNUAL")
	private Integer paymentLimitAnnual = 0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - PG사")
	@Column(name="FEE_PG")
	private Double feePg = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 본사")
	@Column(name="FEE_HEAD")
	private Double feeHead = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 지사")
	@Column(name="FEE_BRANCH")
	private Double feeBranch = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 대리점")
	@Column(name="FEE_AGENCY")
	private Double feeAgency = 0.0;
	
	@ApiModelProperty(notes="최소결제금액")
	@Column(name="MIN_PAYMENT_AMOUNT")
	private Integer minPaymentAmt = 0;
	
	// TODO 순간정산 수수료 추가해야함(단위가 %인지 원단위인지 확인 후 DB 데이터형 확인하고 추가할 것)
	
	@ApiModelProperty(notes="본사 가입비")
	@Column(name="MEMBERSHIP_FEE_HEAD")
	private Integer membershipFeeHead = 0;
	
	@ApiModelProperty(notes="지사 가입비")
	@Column(name="MEMBERSHIP_FEE_BRANCH")
	private Integer membershipFeeBranch = 0;
	
	@ApiModelProperty(notes="대리점 가입비")
	@Column(name="MEMBERSHIP_FEE_AGENCY")
	private Integer membershipFeeAgency;
	
	@ApiModelProperty(notes="본사 설치비")
	@Column(name="INSTALLATION_FEE_HEAD")
	private Integer installationFeeHead = 0;
	
	@ApiModelProperty(notes="지사 설치비")
	@Column(name="INSTALLATION_FEE_BRANCH")
	private Integer installationFeeBranch = 0;
	
	@ApiModelProperty(notes="대리점 설치비")
	@Column(name="INSTALLATION_FEE_AGENCY")
	private Integer installationFeeAgency = 0;
	
	@ApiModelProperty(notes="본사 관리비")
	@Column(name="MAINTENANCE_FEE_HEAD")
	private Integer maintenanceFeeHead = 0;
	
	@ApiModelProperty(notes="지사 관리비")
	@Column(name="MAINTENANCE_FEE_BRANCH")
	private Integer maintenanceFeeBranch = 0;
	
	@ApiModelProperty(notes="대리점 관리비")
	@Column(name="MAINTENANCE_FEE_AGENCY")
	private Integer maintenanceFeeAgency = 0;
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="STORE_UID_FK")
	private List<StoreId> storeIds;
	
	@ApiModelProperty(notes="소속 그룹")
	@JsonBackReference(value="group-stores")
	@ManyToOne
	@JoinColumn(name="GROUP_UID_FK")
	private Group group;
	
	@JsonManagedReference(value="store-members")
	@OneToMany(cascade=CascadeType.ALL, mappedBy="store")
	private List<Member> members;
}
