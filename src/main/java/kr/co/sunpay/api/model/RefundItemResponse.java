package kr.co.sunpay.api.model;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModelProperty;
import kr.co.sunpay.api.domain.KsnetRefundLog;
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
	}
}
