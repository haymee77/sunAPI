package kr.co.sunpay.api.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {
	
	private TemplateEngine templateEngine;
	
	private final String PLACEHOLDER_TITLE = "title";
	private final String PLACEHOLDER_CONTENT = "content";
	private final String PLACEHOLDER_SENDER = "sender";
	private final String PLACEHOLDER_CONTACT = "contact";
	private final String PLACEHOLDER_LINK = "link";
	
	@Autowired
	public MailContentBuilder(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public String build(String template, String title, String content, String sender) {
		Context context = new Context();
		
		if (!Sunpay.isEmpty(title)) context.setVariable(PLACEHOLDER_TITLE, title);
		if (!Sunpay.isEmpty(content)) context.setVariable(PLACEHOLDER_CONTENT, content);
		if (!Sunpay.isEmpty(sender)) context.setVariable(PLACEHOLDER_SENDER, sender);
		
		return templateEngine.process(template, context);
		
	}
	
	public String build(String template, String title, String content) {
		Context context = new Context();
		
		if (!Sunpay.isEmpty(title)) context.setVariable(PLACEHOLDER_TITLE, title);
		if (!Sunpay.isEmpty(content)) context.setVariable(PLACEHOLDER_CONTENT, content);
		
		return templateEngine.process(template, context);
		
	}
	
	public String build(String template, String title, String content, String sender, String contact) {
		Context context = new Context();
		
		if (!Sunpay.isEmpty(title)) context.setVariable(PLACEHOLDER_TITLE, title);
		if (!Sunpay.isEmpty(content)) context.setVariable(PLACEHOLDER_CONTENT, content);
		if (!Sunpay.isEmpty(sender)) context.setVariable(PLACEHOLDER_SENDER, sender);
		if (!Sunpay.isEmpty(contact)) context.setVariable(PLACEHOLDER_CONTACT, contact);
		
		return templateEngine.process(template, context);
		
	}
	
	public String build(String template, String title, String content, String sender, String contact, String link) {
		Context context = new Context();
		
		if (!Sunpay.isEmpty(title)) context.setVariable(PLACEHOLDER_TITLE, title);
		if (!Sunpay.isEmpty(content)) context.setVariable(PLACEHOLDER_CONTENT, content);
		if (!Sunpay.isEmpty(sender)) context.setVariable(PLACEHOLDER_SENDER, sender);
		if (!Sunpay.isEmpty(contact)) context.setVariable(PLACEHOLDER_CONTACT, contact);
		if (!Sunpay.isEmpty(contact)) context.setVariable(PLACEHOLDER_LINK, link);
		
		return templateEngine.process(template, context);
		
	}
}
