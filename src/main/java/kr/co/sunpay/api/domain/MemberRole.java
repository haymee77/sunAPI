package kr.co.sunpay.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="SP_MEMBER_ROLES")
public class MemberRole {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@Column(name="ROLE_NM", length=20)
	private String roleName;
}
