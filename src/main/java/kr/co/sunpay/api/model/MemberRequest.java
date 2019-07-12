package kr.co.sunpay.api.model;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.Group;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.MemberRole;
import kr.co.sunpay.api.domain.Store;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequest {

	@NotBlank(message = "아이디를 입력해주세요.")
	@Size(max = 25, message = "아이디는 25글자까지 입력가능합니다.")
	@ApiModelProperty(notes = "아이디")
	private String id;

	private String password;

	@NotBlank(message = "이름을 입력해주세요.")
	@Size(max = 5, message = "이름은 5글자까지 입력가능합니다.")
	@ApiModelProperty(notes = "가입자명")
	private String name;

	@NotBlank(message = "메일을 작성해주세요.")
	@Size(max = 100)
	@Email(message = "메일의 양식을 지켜주세요.")
	@ApiModelProperty(notes = "메일")
	private String email;

	@NotBlank(message = "연락처를 작성해주세요.")
	@Pattern(regexp = "[0-9]{10,11}", message = "10~11자리의 숫자만 입력가능합니다")
	@ApiModelProperty(notes = "연락처")
	private String mobile;

	private Boolean activate;

	//@NotNull(message = "이벤트 메일 수신여부를 선택해주세요.")
	private Boolean agreeEventMail;

	@NotBlank(message = "소속을 선택해주세요.")
	@Size(max = 25)
	@ApiModelProperty(notes = "소속(GROUP, STORE)")
	private String belongTo;

	private Integer storeUid;

	private Integer groupUid;

	@NotEmpty(message = "권한을 1가지 이상 성택해주세요.")
	private List<MemberRole> roles;

	public Member toEntity(Store store) {
		return new Member(id, password, name, email, mobile, activate, agreeEventMail, roles, store);
	}
	
	public Member toEntity(Group group) {
		return new Member(id, password, name, email, mobile, activate, agreeEventMail, roles, group);
	}
}
