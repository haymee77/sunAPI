package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_MEMBERS")
@SQLDelete(sql="UPDATE SP_MEMBERS SET DELETED=1 WHERE UID=?")
@Where(clause="DELETED<>1")
@ToString
public class Member extends BaseEntity {
	
	@Column(name="ID", length=25)
	private String id;
	
	@Column(name="PASSWORD", length=255)
	private String password;
	
	@ApiModelProperty(hidden=true)
	@Column(name="LOGIN_DT")
	private LocalDateTime loginDate;
	
	@Column(name="NAME", length=50)
	private String name;
	
	@Column(name="EMAIL", length=200)
	private String email;
	
	@Column(name="MOBILE", length=20)
	private String mobile;
	
	@Column(name="ACTIVATE", columnDefinition="BIT(1)", insertable=false)
	private Boolean activate;
	
	@Column(name="IS_AGREE_EVENT_MAIL", columnDefinition="BIT(1)")
	private Boolean agreeEventMail;
	
	@JsonBackReference(value="store-members")
	@ManyToOne
	@JoinColumn(name="STORE_UID_FK")
	private Store store;

	@JsonBackReference(value="group-members")
	@ManyToOne
	@JoinColumn(name="GROUP_UID_FK")
	private Group group;
	
	// API 상태 코드 (2019-07-01:JAEROX)
	@Column(name="API_STATUS_CD", columnDefinition="CHAR")
	private String apiStatusCd;
		
	// 사업장명
	@Column(name="API_BIZ_NM", length=50)
	private String apiBizNm;
		
	// 사업자 종류
	@Column(name="API_BIZ_TYPE_CD", length=20)
	private String apiBizTypeCd;
		
	// API 결제(PG)수수료 - 대리점(%단위) (2019-07-01:JAEROX)
	@Column(name="API_FEE")
	private Double apiFee = 0.0;
		
	// API 순간정산 건당 송금수수료 - 대리점 (2019-07-01:JAEROX)
	@Column(name="API_TRANS_FEE")
	private Integer apiTransFee = 0;
	
	@ApiModelProperty(notes="TOP(최고관리자), HEAD(본사 권한), BRANCH(지사 권한),  AGENCY(대리점 권한), STORE(가맹점 권한), MANAGER(본사, 지사, 대리점, 상점의 대표/기본 계정), STAFF(본사, 지사, 대리점, 상점의 부계정), CS(고객관리 권한), DEV(개발자 권한)")
	//@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="MEMBER_UID_FK")
	private List<MemberRole> roles;
	
	public Member() {}
	
	public Member(String id, String password, String name, String email, String mobile, Boolean activate, Boolean agreeEventMail, List<MemberRole> roles, Store store) {
		this.id = id;
		this.password = password;
		this.name = name;
		this.email = email;
		this.mobile = mobile;
		this.activate = activate;
		this.agreeEventMail = agreeEventMail;
		this.roles = roles;
		this.store = store;
	}
	
	public Member(String id, String password, String name, String email, String mobile, Boolean activate, Boolean agreeEventMail, List<MemberRole> roles, Group group) {
		this.id = id;
		this.password = password;
		this.name = name;
		this.email = email;
		this.mobile = mobile;
		this.activate = activate;
		this.agreeEventMail = agreeEventMail;
		this.roles = roles;
		this.group = group;
	}
	
	public Member(String id, String password, String name, String email, String mobile, Boolean activate, Boolean agreeEventMail, List<MemberRole> roles) {
		this.id = id;
		this.password = password;
		this.name = name;
		this.email = email;
		this.mobile = mobile;
		this.activate = activate;
		this.agreeEventMail = agreeEventMail;
		this.roles = roles;
	}
}
