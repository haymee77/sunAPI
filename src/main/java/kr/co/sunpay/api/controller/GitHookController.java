package kr.co.sunpay.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping("/git")
public class GitHookController {

	@GetMapping("/push/{project}")
	public ResponseEntity<Object> push(@PathVariable("project") String project) {
		
		System.out.println(project);
		
		return ResponseEntity.ok().build();
	}
}
