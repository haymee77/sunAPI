package kr.co.sunpay.api.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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

	/**
	 * Gitlab 에서 Push event 발생 시 호출되는 API
	 * 
	 * @param project
	 * @param body
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/push/{project}")
	public ResponseEntity<Object> pushListener(@PathVariable("project") String project,
			@RequestBody Map<String, Object> body) throws IOException {

		System.out.println("pushListener...");
		String command = "sudo /home/ubuntu/app/" + project + "/deploy.sh";
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.start();

		return ResponseEntity.ok().build();
	}
}