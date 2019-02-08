package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="SP_FCM_TOKENS")
@Where(clause = "DELETED<>1")
@SQLDelete(sql = "UPDATE SP_TOKENS SET DELETED=1 WHERE UID=?")
public class FCMToken extends BaseEntity {

	@ApiModelProperty(notes="토큰 만료일, API에서 생성됨")
	@Column(name="EXPIRED_DT")
	private LocalDateTime expiredDate;
	
	@ApiModelProperty(required=true)
	@Column(name="ID", length=25)
	private String id;
	
	@ApiModelProperty(required=true)
	@Column(name="PASSWORD", length=255)
	private String password;
	
	@Column(name="EMAIL", length=200)
	private String email;
	
	@ApiModelProperty(notes="앱 로그인 시 PUSH 수신 동의 시 FCM TOKEN 값")
	@Column(name="FCM_TOKEN", length=200)
	private String fcmToken;
	
	@ApiModelProperty(notes="토큰 생성 성공/실패 여부")
	@Column(name="SUCCESS_FL", columnDefinition="BIT(1) DEFAULT 0")
	private Boolean success = false;
	
	@ApiModelProperty(notes="FCM TOKEN 으로 PUSH 테스트 결과값")
	@Column(name="FCM_RETURNS", length=200)
	private String fcmReturns;
	
	@ApiModelProperty(notes="토큰 요청 API로 생성된 로그인 토큰")
	@Transient
	private String loginToken;
}
