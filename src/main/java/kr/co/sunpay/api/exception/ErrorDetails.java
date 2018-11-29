package kr.co.sunpay.api.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * REST API 에러 리턴 타입
 * @author himeepark
 *
 */
@Setter
@Getter
public class ErrorDetails {

	private HttpStatus status;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd hh:mm:ss")
	private LocalDateTime timestamp;
	private String message;
	private String debugMessage;
	
	private ErrorDetails() {
		timestamp = LocalDateTime.now();
	}
	
	ErrorDetails(HttpStatus status) {
		this();
		this.status = status;
	}
	
	ErrorDetails(HttpStatus status, Throwable ex) {
		this();
		this.status = status;
		this.message = "Unexcepted error";
		this.debugMessage = ex.getLocalizedMessage();
	}
	
	ErrorDetails(HttpStatus status, String message, Throwable ex) {
		this();
		this.status = status;
		this.message = message;
		this.debugMessage = ex.getLocalizedMessage();
	}
}
