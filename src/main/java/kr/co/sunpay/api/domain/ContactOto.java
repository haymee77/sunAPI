package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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

	@Column(name="ANSWERED_DT")
	private LocalDateTime answeredDate;
	
	@Column(name="WRITER", length=10)
	private String writer;

	@Column(name="CONTACT", length=15)
	private String contact;
	
	@Column(name="DUTY", length=30)
	private String duty;
	
	@Column(name="COMPANY", length=30)
	private String company;
	
	@Column(name="MAIL", length=200)
	private String mail;
	
	@Column(name="URL", length=200)
	private String url;
	
	@Column(name="TYPE_CD", length=40)
	private String typeCode;
	
	@Column(name="QUERY", columnDefinition="TEXT")
	private String query;
	
	@Column(name="STATUS_CD", length=40)
	private String statusCode;
	
	@Column(name="TITLE", length=45)
	private String title;
	
	@Column(name="ANSWER", columnDefinition="TEXT")
	private String answer;
	
	public ContactOto() {}
	
	public ContactOto(String writer, String contact, String duty, String company, String mail, String url,
			String typeCode, String query, String statusCode, String title, String answer) {
		this.writer = writer;
		this.contact = contact;
		this.duty = duty;
		this.company = company;
		this.mail = mail;
		this.url = url;
		this.typeCode = typeCode;
		this.query = query;
		this.statusCode = statusCode;
		this.title = title;
		this.answer = answer;
	}
}
