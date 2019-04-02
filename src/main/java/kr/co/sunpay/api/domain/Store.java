package kr.co.sunpay.api.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.model.MemberRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import springfox.documentation.annotations.ApiIgnore;

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
	private Integer deposit = 0;
	
	@ApiModelProperty(notes="최소취소예치금")
	@Column(name="MIN_DEPOSIT")
	private int minDeposit;
	
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
	
	@Column(name="BIZ_MAIL", length=200)
	private String bizMail;
	
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
	
	@ApiModelProperty(notes="건당 송금수수료 - PG")
	@Column(name="TRANS_FEE_PG")
	private Integer transFeePg = 0;
	
	@ApiModelProperty(notes="건당 송금수수료 - 본사")
	@Column(name="TRANS_FEE_HEAD")
	private Integer transFeeHead = 0;
	
	@ApiModelProperty(notes="건당 송금수수료 - 지사")
	@Column(name="TRANS_FEE_BRANCH")
	private Integer transFeeBranch = 0;
	
	@ApiModelProperty(notes="건당 송금수수료 - 대리점")
	@Column(name="TRANS_FEE_AGENCY")
	private Integer transFeeAgency = 0;
	
	@ApiModelProperty(notes="가입비")
	@Column(name="MEMBERSHIP_FEE")
	private Integer membershipFee = 0;
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="STORE_UID_FK")
	private List<StoreId> storeIds;
	
	@ApiModelProperty(notes="소속 그룹", hidden=true)
	@JsonBackReference(value="group-stores")
	@ManyToOne
	@JoinColumn(name="GROUP_UID_FK")
	private Group group;
	
	@JsonManagedReference(value="store-members")
	@OneToMany(cascade=CascadeType.ALL, mappedBy="store")
	@Fetch(FetchMode.SUBSELECT)
	@OrderBy("UID DESC")
	@ApiModelProperty(hidden=true)
	private List<Member> members;
	
	@ApiModelProperty(notes="[Transient] 담당자")
	@Transient
	private MemberRequest memberReq;
	
	@ApiModelProperty(notes="*[READ_ONLY]* 소속 그룹의 상호명")
	@Transient
	private String groupName;
	
	@ApiModelProperty(notes="*[READ_ONLY]* 소속 그룹 UID")
	@Transient
	private int groupUid;
	
	public Store() {}

	public Store hideFee() {
		
		setFeePg(getFeePg() + getFeeHead() + getFeeBranch() + getFeeAgency());
		setTransFeePg(getTransFeePg() + getTransFeeHead() + getTransFeeBranch() + getTransFeeAgency());
		
		setFeeHead(0.0);
		setFeeBranch(0.0);
		setFeeAgency(0.0);
		setTransFeeHead(0);
		setTransFeeBranch(0);
		setTransFeeAgency(0);
		
		return this;
	}

	/**
	 * 현재 활성화된 상점ID
	 * @return
	 */
	public StoreId getActivatedId() {
		
		List<StoreId> ids = getStoreIds();
		
		if (!isEmpty(ids)) {
			for (StoreId id : ids) {
				if (id.getActivated()) {
					return id;
				}
			}
		}
		
		return null;
	}
}
