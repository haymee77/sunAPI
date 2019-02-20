package kr.co.sunpay.api.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;
import kr.co.sunpay.api.service.GroupService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_GROUPS")
@Where(clause="DELETED<>1")
@SQLDelete(sql="UPDATE SP_GROUPS SET DELETED=1 WHERE UID=?")
@ApiModel
@ToString
public class Group extends BaseEntity {
	
	@ApiModelProperty(notes="상위 그룹 UID", required=true, position=1)
	@Column(name="PARENT_GROUP_UID")
	private Integer parentGroupUid;
	
	@ApiModelProperty(notes="*[READ_ONLY]* 상위 그룹 이름", position=2)
	@Transient
	private String parentBizName;
	
	@ApiModelProperty(notes="그룹 권한 코드")
	@Column(name="ROLE_CD", length=20)
	private String roleCode;
	
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
	
	@ApiModelProperty(notes="사업자 등록번호")
	@Column(name="BIZ_NO", length=15)
	private String bizNo;
	
	@ApiModelProperty(notes="사업자 상호명")
	@Column(name="BIZ_NM", length=50)
	private String bizName;
	
	@ApiModelProperty(notes="사업자(비사업자) 성명")
	@Column(name="BIZ_OWNER", length=10)
	private String bizOwner;
	
	@ApiModelProperty(notes="사업자(비사업자) 주민번호")
	@Column(name="BIZ_OWNER_REGI_NO", length=15)
	private String bizOwnerRegiNo;
	
	@ApiModelProperty(notes="사업자(비사업자) 우편번호")
	@Column(name="BIZ_ZIPCODE", length=10)
	private String bizZipcode;
	
	@ApiModelProperty(notes="사업자(비사업자) 주소-기본")
	@Column(name="BIZ_ADDRESS_BASIC", length=100)
	private String bizAddressBasic;
	
	@ApiModelProperty(notes="사업자(비사업자) 주소-상세")
	@Column(name="BIZ_ADDRESS_DETAIL", length=100)
	private String bizAddressDetail;
	
	@ApiModelProperty(notes="사업자(비사업자) 연락처")
	@Column(name="BIZ_CONTACT", length=25)
	private String bizContact;
	
	@ApiModelProperty(notes="업종")
	@Column(name="BIZ_INDUSTRY", length=200)
	private String bizIndustry;
	
	@ApiModelProperty(notes="업태")
	@Column(name="BIZ_STATUS", length=200)
	private String bizStatus;
	
	@ApiModelProperty(notes="*[READ_ONLY]* 기본사용자")
	@Transient
	private int ownerMemberUid;
	
	@ApiModelProperty(notes="*[READ_ONLY]* PG수수료(%단위) - PG사", accessMode=AccessMode.READ_ONLY)
	@Column(name="FEE_PG")
	private Double feePg = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 지사 등록 시 필수값(본사에 할당될 수수료)")
	@Column(name="FEE_HEAD")
	private Double feeHead = 0.0;
	
	@ApiModelProperty(notes="PG수수료(%단위) - 대리점 등록 시 필수값(지사에 할당될 수수료)")
	@Column(name="FEE_BRANCH")
	private Double feeBranch = 0.0;
	
	@ApiModelProperty(notes="*[READ_ONLY]* 건당 송금수수료 - PG", accessMode=AccessMode.READ_ONLY)
	@Column(name="TRANS_FEE_PG")
	private Integer transFeePg = 0;
	
	@ApiModelProperty(notes="건당 송금수수료 - 지사 등록 시 필수값(본사에 할당될 수수료)")
	@Column(name="TRANS_FEE_HEAD")
	private Integer transFeeHead = 0;
	
	@ApiModelProperty(notes="건당 송금수수료 - 대리점 등록 시 필수값(지사에 할당될 수수료)")
	@Column(name="TRANS_FEE_BRANCH")
	private Integer transFeeBranch = 0;
	
	@JsonManagedReference(value="group-members")
	@OneToMany(cascade=CascadeType.ALL, mappedBy="group", fetch=FetchType.EAGER, orphanRemoval=true)
	@Fetch(FetchMode.SUBSELECT)
	@OrderBy("UID DESC")
	private List<Member> members;
	
	@JsonManagedReference(value="group-stores")
	@OneToMany(mappedBy="group")
	private List<Store> stores;

	/** 
	 * 수수료 정보 노출 막기위함
	 * @return
	 */
	public Group hideFee() {
		
		switch (this.roleCode) {
		case GroupService.ROLE_BRANCH:
			setFeePg(this.feePg + this.feeHead);
			setTransFeePg(this.transFeePg + this.transFeeHead);
			setFeeHead(0.0);
			setTransFeeHead(0);
			break;
			
		case GroupService.ROLE_AGENCY:
			setFeePg(this.feePg + this.feeHead + this.feeBranch);
			setTransFeePg(this.transFeePg + this.transFeeHead + this.transFeeBranch);
			setFeeHead(0.0);
			setTransFeeHead(0);
			setFeeBranch(0.0);
			setTransFeeBranch(0);
			break;
		}
		return this;
	}
}
