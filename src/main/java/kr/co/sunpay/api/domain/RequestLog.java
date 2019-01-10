package kr.co.sunpay.api.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="SP_API_REQUEST_LOGS")
@ToString
public class RequestLog {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="UID")
	private int uid;
	
	@Column(name="CREATED_DT")
	@CreationTimestamp
	private LocalDateTime createdDate;
	
	@Column(name="IP", length=15)
	private String ip;
	
	@Column(name="URI", length=200)
	private String uri;
	
	@Column(name="METHOD", length=10)
	private String method;
	
	@Column(name="HEADER", length=200)
	private String header;
	
	@Column(name="PARAMETERS", length=200)
	private String params;
}
