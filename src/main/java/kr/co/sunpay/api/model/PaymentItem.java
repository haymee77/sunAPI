package kr.co.sunpay.api.model;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.KsnetPay;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentItem {

	@ApiModelProperty(notes="결제일(PG사 기준)")
	private String paidDate;
	
	@ApiModelProperty(value="결제금액(PG사 실제 결제금액)")
	private int amount = 0;
	
	@ApiModelProperty(notes="서비스 타입 코드(CODE API(GROUP_NM=SERVICE_TYPE) 참조)")
	private String serviceTypeCode;
	
	@ApiModelProperty(notes="PG사 결제번호(영수증번호)")
	private String trNo;
	
	@ApiModelProperty(notes="결제방법(코드)")
	private String paymethodCode;
	
	@ApiModelProperty(notes="상품명(쇼핑몰에서 넘어온 값)")
	private String goodsName;
	
	@ApiModelProperty(notes="주문자명")
	private String orderName;
	
	@ApiModelProperty(notes="상점 주문번호")
	private String orderNo;
	
	@ApiModelProperty(notes="소속레벨")
	private String groupRoleName;
	
	@ApiModelProperty(notes="그룹명")
	private String groupBizName;
	
	@ApiModelProperty(notes="상점명")
	private String storeBizeName;
	
	
	@ApiModelProperty(notes="영수증 번호(신용카드)")
	private String cbtrno;
	
	@ApiModelProperty(notes="승인번호(신용카드)")
	private String authno;
	
	@ApiModelProperty(notes="소유자 이름")
	private String bizOwner;
	
	@ApiModelProperty(notes="owner 권한을 갖는 상점 멤버의  아이디")
	private String ownerMemberId;
	
	@ApiModelProperty(notes="사업장 연락처")
	private String bizContact;
	
	@ApiModelProperty(notes="구매자 연락처")
	private String sndMobile;
	
	@ApiModelProperty(notes="할부")
	private String halbu;
	
	@ApiModelProperty(notes="발급사명")
	private String msg1;
	
	// fee 추가 시작
	@ApiModelProperty(value="PG사수익")
	private Integer profitPg = 0;
	
	@ApiModelProperty(value=" 본사수익")
	private Integer profitHead = 0;
	
	@ApiModelProperty(value="지사수익")
	private Integer profitBranch = 0;
	
	@ApiModelProperty(value="대리점수익")
	private Integer profitAgency = 0;
	
	@ApiModelProperty(value="상점정산액")
	private Integer profitStore = 0;
	
	@ApiModelProperty(value="KSNet으로 전달될 결제데이터")
	private KsnetPay ksnetPay;
	// fee 추가 끝
	
	// 환불 추가 시작
	@ApiModelProperty(value="환불전 상점정산액")
	private Integer beforeRefundProfitStore = 0;
	
    /*	
	@ApiModelProperty(value="상점차감액")
	private Integer storeDeduction = 0;*/
	
	@ApiModelProperty(value=" 취소예치금차감액")
	private Integer depositDeduction = 0;
	
	@ApiModelProperty(value="취소완료일")
	private LocalDateTime refundDateTime;
	// 환불 추가 끝

}
