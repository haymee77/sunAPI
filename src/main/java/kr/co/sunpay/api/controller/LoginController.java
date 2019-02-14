package kr.co.sunpay.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Controller
public class LoginController {

	@GetMapping("/accessDenied")
	public void accessDenied() {
		
	}
	
	@GetMapping("/login")
	public void login() {
		
	}
}
