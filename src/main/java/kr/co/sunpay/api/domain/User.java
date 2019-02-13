package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_USERS")
@SQLDelete(sql="UPDATE SP_USERS SET DELETED=1 WHERE UID=?")
@Where(clause="DELETED<>1")
@ToString
public class User extends BaseEntity {
	
	@Column(name="USERNAME", length=25)
	private String username;
	
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
	
	@ApiModelProperty(notes="TOP(최고관리자), USER(일반유저-읽기, 쓰기 가능), READER(읽기만 가능한 유저)")
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	@JoinColumn(name="USER_UID_FK")
	private List<UserRole> roles;
}
