package kr.co.sunpay.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mail")
public class MailViewController {

	@GetMapping("/defaultTemplate")
	public void defaultTemplate() {
		
	}
	
	@GetMapping("/contactOto/answerTemplate")
	public void answerTemplate() {
		
	}
	
	@GetMapping("/contactOto/queryTemplate")
	public void queryTemplate() {
		
	}
}
