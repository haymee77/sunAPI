package kr.co.sunpay.api.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.ContactOto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactOtoRequest {

	@ApiModelProperty(hidden=true)
	private int uid;
	
	@NotBlank(message="작성자명을 입력해주세요.")
	@Size(max=5, message="작성자명은 5글자까지 입력가능합니다.")
	@ApiModelProperty(notes="작성자명")
	private String writer;
	
	@NotBlank(message="연락처를 작성해주세요.")
	@Pattern(regexp = "[0-9]{10,11}", message = "10~11자리의 숫자만 입력가능합니다")
	@ApiModelProperty(notes="연락처")
	private String contact;
	
	@NotBlank(message="직책을 작성해주세요.")
	@Size(max=15)
	@ApiModelProperty(notes="직책")
	private String duty;
	
	@NotBlank(message="회사명을 작성해주세요.")
	@Size(max=25)
	@ApiModelProperty(notes="회사명")
	private String company;
	
	@NotBlank(message="메일을 작성해주세요.")
	@Size(max=100)
	@Email(message="메일의 양식을 지켜주세요.")
	@ApiModelProperty(notes="메일")
	private String mail;
	
	@NotBlank(message="홈페이지/쇼핑몰 주소를 작성해주세요.")
	@Pattern(regexp="^(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?$", message="URL 형식을 지켜주세요.")
	@ApiModelProperty(notes="홈페이지/쇼핑몰 주소")
	private String url;
	
	@NotBlank(message="문의 유형을 선택해주세요.")
	@ApiModelProperty(notes="문의 유형")
	private String typeCode;
	
	@NotBlank(message="문의내용을 작성해주세요.")
	@ApiModelProperty(notes="문의 내용")
	private String query;
	
	@ApiModelProperty(notes="상태 코드")
	private String statusCode;
	
	@NotBlank(message="제목을 작성해주세요.")
	@Size(max=45)	
	@ApiModelProperty(notes="문의 제목")
	private String title;
	
	@ApiModelProperty(notes="답변 내용")
	private String answer;
	
	public ContactOto toEntity() {
		return new ContactOto(writer, contact, duty, company, mail, url, typeCode, query, statusCode, title, answer);
	}
}
