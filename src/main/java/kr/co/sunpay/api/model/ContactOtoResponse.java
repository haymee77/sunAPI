package kr.co.sunpay.api.model;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.ContactOto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactOtoResponse {

	private int uid;
	
	@ApiModelProperty(notes="생성일")
	private LocalDateTime createdDate;
	
	@ApiModelProperty(notes="수정일")
	private LocalDateTime updatedDate;
	
	@ApiModelProperty(notes="답변일")
	private LocalDateTime answeredDate;
	
	@ApiModelProperty(notes="작성자명")
	private String writer;
	
	@ApiModelProperty(notes="연락처")
	private String contact;
	
	@ApiModelProperty(notes="직책")
	private String duty;
	
	@ApiModelProperty(notes="회사명")
	private String company;
	
	@ApiModelProperty(notes="메일")
	private String mail;
	
	@ApiModelProperty(notes="홈페이지/쇼핑몰 주소")
	private String url;
	
	@ApiModelProperty(notes="문의 유형")
	private String typeCode;
	
	private String query;
	
	@ApiModelProperty(notes="상태 코드")
	private String statusCode;
	
	@ApiModelProperty(notes="문의 제목")
	private String title;
	
	@ApiModelProperty(notes="답변 내용")
	private String answer;
	
	@ApiModelProperty(notes="답변 작성인 UID")
	private Integer answererUid;
	
	public ContactOtoResponse() {}
	
	public ContactOtoResponse(ContactOto contactOto) {
		uid = contactOto.getUid();
		createdDate = contactOto.getCreatedDate();
		updatedDate = contactOto.getUpdatedDate();
		answeredDate = contactOto.getAnsweredDate();
		writer = contactOto.getWriter();
		contact = contactOto.getContact();
		duty = contactOto.getDuty();
		company = contactOto.getCompany();
		mail = contactOto.getMail();
		url = contactOto.getUrl();
		typeCode = contactOto.getTypeCode();
		query = contactOto.getQuery();
		statusCode = contactOto.getStatusCode();
		title = contactOto.getTitle();
		answer = contactOto.getAnswer();
		answererUid = contactOto.getAnswererUid();
	}
}
