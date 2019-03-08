package kr.co.sunpay.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import kr.co.sunpay.api.util.Mail;
import kr.co.sunpay.api.util.MailContentBuilder;

@Service
public class MailService {

	public final static String SUPPORT_SENDER = "support@sunpay.co.kr";
	public final static String SUPPORT_RECEIVER = "hmpark0502@gmail.com";
	
	@Autowired
	private JavaMailSender emailSender;
	
	@Autowired
	public MailContentBuilder mailContentBuilder;
	
	public void sendSimpleMessage(final Mail mail) {
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setSubject(mail.getSubject());
		message.setText(mail.getContent());
		message.setTo(mail.getTo());
		message.setFrom(SUPPORT_SENDER);
		
		emailSender.send(message);
	}
	
	public void sendMail(final Mail mail) {
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
			messageHelper.setFrom(SUPPORT_SENDER);
			messageHelper.setTo(mail.getTo());
			messageHelper.setSubject(mail.getSubject());
			messageHelper.setText(mail.getContent(), true);
		};
		
		try {
			emailSender.send(messagePreparator);
		} catch (MailException e) {
			e.printStackTrace();
		}
	}
}
