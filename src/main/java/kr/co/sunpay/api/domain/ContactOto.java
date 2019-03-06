package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name="SP_CONTACT_OTO")
@Where(clause="DELETED<>1")
@SQLDelete(sql="UPDATE SP_CONTACT_OTO SET DELETED=1 WHERE UID=?")
@ToString
public class ContactOto extends BaseEntity {

	@ApiModelProperty(hidden=true)
	@Column(name="ANSWERED_DT")
	private LocalDateTime answeredDate;
	
	@ApiModelProperty(notes="작성자명")
	@Column(name="WRITER", length=10)
	private String writer;
	
	@ApiModelProperty(notes="연락처")
	@Column(name="CONTACT", length=15)
	private String contact;
	
	@ApiModelProperty(notes="직책")
	@Column(name="DUTY", length=30)
	private String duty;
	
	@ApiModelProperty(notes="작성자 회사명")
	@Column(name="COMPANY", length=30)
	private String company;
	
	@ApiModelProperty(notes="작성자 메일")
	@Column(name="MAIL", length=200)
	private String mail;
	
	@ApiModelProperty(notes="작성자 홈페이지/쇼핑몰 URL")
	@Column(name="URL", length=200)
	private String url;
	
	@ApiModelProperty(notes="문의유형")
	@Column(name="TYPE_CD", length=40)
	private String typeCode;
	
	@ApiModelProperty(notes="문의내용")
	@Column(name="QUERY", columnDefinition="TEXT")
	private String query;
	
	@ApiModelProperty(notes="문의 상태 코드")
	@Column(name="STATUS_CD", length=40)
	private String statusCode;
	
	@ApiModelProperty(notes="문의 제목")
	@Column(name="TITLE", length=45)
	private String title;
	
	@ApiModelProperty(notes="답변내용")
	@Column(name="ANSWER", columnDefinition="TEXT")
	private String answer;
}
