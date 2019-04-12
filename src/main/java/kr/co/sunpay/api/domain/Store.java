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
	
	@Column(name="ACTIVATE", columnDefinition="BIT(1)", insertable=false)
	private Boolean activate;
	
	@Column(name="STATE_CD", length=25)
	private String stateCode;

	@Column(name="BIZ_TYPE_CD", length=20)
	private String bizTypeCode;
	
	@Column(name="BANK_CD", length=20)
	private String bankCode;
	
	@Column(name="BANK_ACCOUNT_NO", length=45)
	private String bankAccountNo;
	
	@Column(name="BANK_ACCOUNT_NM", length=10)
	private String bankAccountName;
	
	@ApiModelProperty(notes="취소예치금 금액")
	@Column(name="DEPOSIT")
	private Integer deposit = 0;
	
	@Column(name="MIN_DEPOSIT")
	private int minDeposit;
	
	@Column(name="DEPOSIT_NO", length=10)
	private String depositNo;
	
	@Column(name="BIZ_NO", length=15)
	private String bizNo;
	
	@Column(name="BIZ_NM", length=50)
	private String bizName;
	
	@Column(name="BIZ_OWNER", length=10)
	private String bizOwner;
	
	@Column(name="BIZ_OWNER_REGI_NO", length=15)
	private String bizOwnerRegiNo;
	
	@Column(name="BIZ_ZIPCODE", length=10)
	private String bizZipcode;
	
	@Column(name="BIZ_ADDRESS_BASIC", length=100)
	private String bizAddressBasic;
	
	@Column(name="BIZ_ADDRESS_DETAIL", length=100)
	private String bizAddressDetail;
	
	@Column(name="BIZ_CONTACT", length=25)
	private String bizContact;
	
	@Column(name="BIZ_INDUSTRY", length=200)
	private String bizIndustry;
	
	@Column(name="BIZ_STATUS", length=200)
	private String bizStatus;
	
	@Column(name="BIZ_MAIL", length=200)
	private String bizMail;
	
	@Column(name="MAX_INSTALLMENT_LIMIT")
	private Integer maxInstallmentLimit;
	
	@Column(name="STORE_URL", length=200)
	private String storeUrl;
	
	@Column(name="SURETY_INSURANCE_FL", columnDefinition="BIT(1) DEFAULT 0")
	private Boolean suretyInsurance = false;
	
	@Column(name="SURETY_INSURANCE_AMOUNT", columnDefinition="DEFAUNT 0")
	private Integer suretyInsuranceAmt = 0;
	
	@Column(name="PAYMENT_LIMIT_ONCE")
	private Integer paymentLimitOnce = 0;
	
	@Column(name="PAYMENT_LIMIT_DAILY")
	private Integer paymentLimitDaily = 0;
	
	@Column(name="PAYMENT_LIMIT_MONTHLY")
	private Integer paymentLimitMonthly = 0;
	
	@Column(name="PAYMENT_LIMIT_QUARTERLY")
	private Integer paymentLimitQuarterly = 0;
	
	@Column(name="PAYMENT_LIMIT_ANNUAL")
	private Integer paymentLimitAnnual = 0;
	
	@Column(name="FEE_PG")
	private Double feePg = 0.0;
	
	@Column(name="FEE_HEAD")
	private Double feeHead = 0.0;

	@Column(name="FEE_BRANCH")
	private Double feeBranch = 0.0;

	@Column(name="FEE_AGENCY")
	private Double feeAgency = 0.0;
	
	@Column(name="MIN_PAYMENT_AMOUNT")
	private Integer minPaymentAmt = 0;
	
	@Column(name="TRANS_FEE_PG")
	private Integer transFeePg = 0;

	@Column(name="TRANS_FEE_HEAD")
	private Integer transFeeHead = 0;

	@Column(name="TRANS_FEE_BRANCH")
	private Integer transFeeBranch = 0;

	@Column(name="TRANS_FEE_AGENCY")
	private Integer transFeeAgency = 0;
	
	@Column(name="MEMBERSHIP_FEE")
	private Integer membershipFee = 0;
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="STORE_UID_FK")
	private List<StoreId> storeIds;
	
	@JsonBackReference(value="group-stores")
	@ManyToOne
	@JoinColumn(name="GROUP_UID_FK")
	private Group group;
	
	@JsonManagedReference(value="store-members")
	@OneToMany(cascade=CascadeType.ALL, mappedBy="store")
	@Fetch(FetchMode.SUBSELECT)
	@OrderBy("UID DESC")
	private List<Member> members;
	
	@Transient
	private String groupName;
	
	@Transient
	private int groupUid;
	
	public Store() {}
	
	public Store(String bizOwner, String bizOwnerRegiNo, String bizNo, String bizTypeCode, String bizName, String bizZipcode, String bizAddressBasic,
		String bizAddressDetail, String bizMail, String bizContact, String bizStatus, String bizIndustry, String storeUrl, Boolean suretyInsurance,
		Integer suretyInsuranceAmt, String depositNo, int minDeposit, int maxInstallmentLimit, Integer minPaymentAmt, String bankCode, String bankAccountNo,
		String bankAccountName, Integer paymentLimitOnce, Integer paymentLimitDaily, Integer paymentLimitMonthly, Integer paymentLimitQuarterly,
		Integer paymentLimitAnnual, Double feePg, Double feeHead, Double feeBranch, Double feeAgency, Integer transFeePg, Integer transFeeHead, Integer transFeeBranch,
		Integer transFeeAgency, Integer membershipFee) {
		
		this.bizOwner = bizOwner;
		this.bizOwnerRegiNo = bizOwnerRegiNo;
		this.bizNo = bizNo;
		this.bizTypeCode = bizTypeCode;
		this.bizName = bizName;
		this.bizAddressBasic = bizAddressBasic;
		this.bizAddressDetail = bizAddressDetail;
		this.bizMail = bizMail;
		this.bizContact = bizContact;
		this.bizStatus = bizStatus;
		this.bizIndustry = bizIndustry;
		this.storeUrl = storeUrl;
		this.suretyInsurance = suretyInsurance;
		this.suretyInsuranceAmt = suretyInsuranceAmt;
		this.depositNo = depositNo;
		this.minDeposit = minDeposit;
		this.maxInstallmentLimit = maxInstallmentLimit;
		this.minPaymentAmt = minPaymentAmt;
		this.bankCode = bankCode;
		this.bankAccountNo = bankAccountNo;
		this.bankAccountName = bankAccountName;
		this.paymentLimitOnce = paymentLimitOnce;
		this.paymentLimitDaily = paymentLimitDaily;
		this.paymentLimitMonthly = paymentLimitMonthly;
		this.paymentLimitQuarterly = paymentLimitQuarterly;
		this.paymentLimitAnnual = paymentLimitAnnual;
		this.feePg = feePg;
		this.feeHead = feeHead;
		this.feeBranch = feeBranch;
		this.feeAgency = feeAgency;
		this.transFeePg = transFeePg;
		this.transFeeHead = transFeeHead;
		this.transFeeBranch = transFeeBranch;
		this.transFeeAgency = transFeeAgency;
		this.membershipFee = membershipFee;
	}
	
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
	@ApiIgnore
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
