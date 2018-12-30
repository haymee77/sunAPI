package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_MEMBERS")
@SQLDelete(sql="UPDATE SP_MEMBERS SET DELETED=1 WHERE UID=?")
@Where(clause="DELETED<>1")
@EqualsAndHashCode(of="uid")
@ToString
public class Member {

	@ApiModelProperty(hidden=true)
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@ApiModelProperty(hidden=true)
	@Column(name="CREATED_DT")
	@CreationTimestamp
	private LocalDateTime createdDate;
	
	@ApiModelProperty(hidden=true)
	@Column(name="UPDATED_DT")
	@UpdateTimestamp
	private LocalDateTime updatedDate;
	
	@ApiModelProperty(hidden=true)
	@Column(name="DELETED", columnDefinition="BIT(1) DEFAULT 0")
	private Boolean deleted = false;
	
	@Column(name="ID", length=25)
	private String id;
	
	@Column(name="PASSWORD", length=255)
	private String password;
	
	@ApiModelProperty(hidden=true)
	@Column(name="LOGIN_DT")
	private LocalDateTime loginDate;
	
	@Column(name="NAME", length=50)
	private String name;
	
	@Column(name="EMAIL", length=60)
	private String email;
	
	@Column(name="MOBILE", length=20)
	private String mobile;
	
	@Column(name="ACTIVATE", columnDefinition="BIT(1)")
	private Boolean activate;
	
	@ApiModelProperty(notes="TOP(최고관리자), HEAD(본사 권한), BRANCH(지사 권한),  AGENCY(대리점 권한), STORE(가맹점 권한), MANAGER(본사, 지사, 대리점, 상점의 대표/기본 계정), STAFF(본사, 지사, 대리점, 상점의 부계정), CS(고객관리 권한), DEV(개발자 권한)")
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	@JoinColumn(name="MEMBER_UID_FK")
	private List<MemberRole> roles;
}
