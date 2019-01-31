package kr.co.sunpay.api.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_GROUPS")
@Where(clause="DELETED<>1")
@SQLDelete(sql="UPDATE SP_GROUPS SET DELETED=1 WHERE UID=?")
@ToString
public class Group extends BaseEntity {
	
	@ApiModelProperty(notes="상위 그룹 UID", required=true)
	@Column(name="PARENT_GROUP_UID")
	private Integer parentGroupUid;
	
	@ApiModelProperty(notes="상위 그룹 이름")
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
	
	@ApiModelProperty(notes="기본사용자")
	@Transient
	private int ownerMemberUid;
	
	@JsonManagedReference(value="group-members")
	@OneToMany(cascade=CascadeType.ALL, mappedBy="group")
	private List<Member> members;
	
	@JsonManagedReference(value="group-stores")
	@OneToMany(mappedBy="group")
	private List<Store> stores;
}
