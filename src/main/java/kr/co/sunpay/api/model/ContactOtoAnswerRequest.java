package kr.co.sunpay.api.model;

import lombok.Setter;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@Setter
public class ContactOtoAnswerRequest {

	@NotBlank(message="답변 내용을 작성해주세요.")
	@ApiModelProperty(notes="답변 내용")
	private String answer; 
}
