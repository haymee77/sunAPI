package kr.co.sunpay.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KspayCancelReturns {

	private String rTransactionNo;
	
	private String rStatus;
	
	private String rTradeDate;
	
	private String rTradeTime;
	
	private String rMessage1;
	
	private String rMessage2;
	
	public KspayCancelReturns() {
		
	}
	
	public KspayCancelReturns(String rTransactionNo, String rStatus, String rTradeDate, String rTradeTime,
			String rMessage1, String rMessage2) {
		
		this.rTransactionNo = rTransactionNo;
		this.rStatus = rStatus;
		this.rTradeDate = rTradeDate;
		this.rTradeTime = rTradeTime;
		this.rMessage1 = rMessage1;
		this.rMessage2 = rMessage2;
	}
}
