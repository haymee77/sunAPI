package kr.co.sunpay.api.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Mail {

	private String to;
	private String subject;
	private String content;
	
	public Mail() {}
	
	public Mail(String to, String subject, String content) {
		this.to = to;
		this.subject = subject;
		this.content = content;
	}
	
}
