package kr.co.sunpay.api.model;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.Member;
import kr.co.sunpay.api.domain.MemberRole;
import kr.co.sunpay.api.util.Sunpay;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponse {
	
	private int uid;
	
	@ApiModelProperty(notes="생성일")
	private LocalDateTime createdDate;
	
	@ApiModelProperty(notes="수정일")
	private LocalDateTime updatedDate;
	
	@ApiModelProperty(notes="답변일")
	private LocalDateTime loginDate;
	
	@ApiModelProperty(notes="아이디")
	private String id;
	
	@ApiModelProperty(notes="이름")
	private String name;
	
	@ApiModelProperty(notes="이메일")
	private String email;
	
	@ApiModelProperty(notes="답변일")
	private String mobile;
	
	@ApiModelProperty(notes="활성화 여부")
	private Boolean activate;
	
	@ApiModelProperty(notes="이벤트 메일 수신여부")
	private Boolean agreeEventMail;
	
	@ApiModelProperty(value="소속 상점 UID")
	private int storeUid;
	
	@ApiModelProperty(value="소속 상점 이름")
	private String storeName;
	
	@ApiModelProperty(value="소속 그룹 UID")
	private int groupUid;
	
	@ApiModelProperty(value="소속 그룹 이름")
	private String groupName;
	
	@ApiModelProperty(notes="TOP(최고관리자), HEAD(본사 권한), BRANCH(지사 권한),  AGENCY(대리점 권한), STORE(가맹점 권한), MANAGER(본사, 지사, 대리점, 상점의 대표/기본 계정), STAFF(본사, 지사, 대리점, 상점의 부계정), CS(고객관리 권한), DEV(개발자 권한)")
	private List<MemberRole> roles;
	
	public MemberResponse() {}
	
	public MemberResponse(Member member) {
		uid = member.getUid();
		createdDate = member.getCreatedDate();
		updatedDate = member.getUpdatedDate();
		loginDate = member.getLoginDate();
		id = member.getId();
		name = member.getName();
		email = member.getEmail();
		mobile = member.getMobile();
		activate = member.getActivate();
		agreeEventMail = member.getAgreeEventMail();
		
		// 상점회원이면 상점데이터 가져오기
		if (!Sunpay.isEmpty(member.getStore())) {
			storeUid = member.getStore().getUid();
			storeName = member.getStore().getBizName();
			
		// 그룹회원이면 그룹데이터 가져오기
		} else if (!Sunpay.isEmpty(member.getGroup())) {
			groupUid = member.getGroup().getUid();
			groupName = member.getGroup().getBizName();
		}
	}
}
