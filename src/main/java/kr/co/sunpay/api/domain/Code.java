package kr.co.sunpay.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_CODES")
@ToString
public class Code extends DefaultEntity {
	
	@ApiModelProperty(notes="코드 그룹명")
	@Column(name="GROUP_NM", length=45)
	private String groupName;
	
	@ApiModelProperty(notes="코드 구분값")
	@Column(name="CODE", length=45)
	private String code;
	
	@ApiModelProperty(notes="코드 이름(노출될 내용)")
	@Column(name="CODE_NM", length=65)
	private String codeName;
	
	@ApiModelProperty(notes="코드 공개/비공개 여부")
	@Column(name="IS_PRIVATE", columnDefinition="BIT(1) DEFAULT FALSE")
	private boolean isPrivate;
	
	@ApiModelProperty(notes="private 코드인 경우 코드를 볼 수 있는 멤버 권한(MEMBER_ROLES), 콤마로 구분함")
	@Column(name="AUTHORIZED")
	private String authorized;
}
