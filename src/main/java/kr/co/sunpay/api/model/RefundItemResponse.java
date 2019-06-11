package kr.co.sunpay.api.model;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.KsnetRefundLog;
import kr.co.sunpay.api.domain.Store;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundItemResponse {

	private int uid;
	
	@ApiModelProperty(notes="환불요청날짜")
	private LocalDateTime refundDate;
	
	@ApiModelProperty(notes="취소금액")
	private Integer amt;
	
	@ApiModelProperty(notes="결제번호")
	private String trNo;
	
	@ApiModelProperty(notes="정산타입")
	private String serviceTypeCode;
	
	@ApiModelProperty(notes="결제수단")
	private String paymethodCode;
	
	@ApiModelProperty(notes="상품명")
	private String goodsName;
	
	@ApiModelProperty(notes="구매자명")
	private String buyerName;
	
	@ApiModelProperty(notes="상점주문번호")
	private String orderNo;
	
	@ApiModelProperty(notes="환불상태")
	private String statusCode;
	
	// 추가작업
	
	@ApiModelProperty(notes="소속레벨")
	private String groupRoleName;
	
	@ApiModelProperty(notes="그룹명")
	private String groupBizName;
	
	@ApiModelProperty(notes="사업장명")
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
	private String Msg1;
	
	public RefundItemResponse() {}
	
	public RefundItemResponse(KsnetRefundLog log) {
		uid = log.getUid();
		refundDate = log.getCreatedDate();
		amt = log.getAmt();
		trNo = log.getTrNo();
		serviceTypeCode = log.getServiceTypeCode();
		paymethodCode = log.getKsnetPayResult().getKsnetPay().getSndPaymethod();
		buyerName = log.getKsnetPayResult().getKsnetPay().getSndOrdername();
		orderNo = log.getKsnetPayResult().getOrdno();
		statusCode = log.getStatusCode();		
		goodsName = log.getKsnetPayResult().getKsnetPay().getSndGoodname();	
	}
}
