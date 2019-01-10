package kr.co.sunpay.api.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

//@ApiIgnore
@RestController
@RequestMapping("/git")
public class GitHookController {
	
	@PostMapping("/push/{project}")
	public ResponseEntity<Object> pushListener(@PathVariable("project") String project, @RequestBody Map<String, Object> body) {
		
		System.out.println(project);
		try {
			System.out.println("process builder start...");
			ProcessBuilder pb = new ProcessBuilder("sudo su", "/home/ubuntu/app/dashboard/deploy.sh");
			pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ResponseEntity.ok().build();
	}
}