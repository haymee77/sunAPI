package kr.co.sunpay.api.service;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepositMsgParser {

	private int amt;
	
	private String depositNo;
	
	private String parsingMsg;
	
	private boolean parsingOk;
	
	public DepositMsgParser(String msg) {
		
		this.parsingOk = true;
		String lines[] = msg.split("\n");
		
		// 국민은행 기준 문자메세지 7줄 체크 
		if (lines.length < 7) {
			this.parsingOk = false;
			this.parsingMsg = "Can not parsing message";
		}
		
		// 국민은행 기준 6번째 줄, 입금액 확인
		try {
			this.amt = NumberFormat.getNumberInstance(Locale.UK).parse(lines[5]).intValue();
		} catch (ParseException ex) {
			this.amt = 0;
			this.parsingOk = false;
			this.parsingMsg = "Can not read Deposit amount";
		}
		
		// 국민은행 기준 4번째 줄, 입금처(SP_STORES.DEPOSIT_NO) 번호는 6자여야함
		if (lines[3].length() == 6) {
			this.depositNo = lines[3];
		} else {
			this.parsingOk = false;
			this.parsingMsg = "Deposit Number is invalid";
		}
	}
}
