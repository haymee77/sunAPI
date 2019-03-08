package kr.co.sunpay.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import kr.co.sunpay.api.service.MailService;
import kr.co.sunpay.api.util.Mail;
import kr.co.sunpay.api.util.MailContentBuilder;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SendMailTest {

	@Autowired
	MailService mailService;
	
	@Autowired
	MailContentBuilder mailContentBuilder;
	
	@Test
	public void sendMail() {
		
		try {
			String content = mailContentBuilder.build("mail/defaultTemplate", "", "메일 테스트입니다.");
			Mail mail = new Mail("haymee77@naver.com", "Default 메일 발송 테스트", content);
			mailService.sendMail(mail);
			
			mail.setTo("hmpark0502@gmail.com");
			mailService.sendMail(mail);
			
			mail.setTo("support@sunpay.co.kr");
			mailService.sendMail(mail);
			
			mail.setTo("heeeunlee3@gmail.com");
			mailService.sendMail(mail);
			
			mail.setTo("sucool125@gmail.com");
			mailService.sendMail(mail);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
