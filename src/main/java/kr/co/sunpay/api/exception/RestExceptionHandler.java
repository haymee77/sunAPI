package kr.co.sunpay.api.exception;

import javax.persistence.EntityNotFoundException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.java.Log;

/**
 * REST API 에러 핸들링 커스터마이징
 * @author himeepark
 *
 */
@Log
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		log.info("-- RestExceptionHandler.ResponseEntity called..");
		String error = "Malformed JSON request";
		return buildResponseEntity(new ErrorDetails(HttpStatus.BAD_REQUEST, error, ex));
	}
	
	@ExceptionHandler(EntityNotFoundException.class)
	protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
		
		ErrorDetails errorDetails = new ErrorDetails(HttpStatus.NOT_FOUND);
		errorDetails.setMessage(ex.getMessage());
		
		return buildResponseEntity(errorDetails);
	}
	
	@ExceptionHandler(BadCredentialsException.class)
	protected ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
		
		ErrorDetails errorDetails = new ErrorDetails(HttpStatus.UNAUTHORIZED);
		errorDetails.setMessage(ex.getMessage());
		
		return buildResponseEntity(errorDetails);
	}
	
	@ExceptionHandler(DuplicateKeyException.class)
	protected ResponseEntity<Object> handleDuplicateKey(DuplicateKeyException ex) {
		
		ErrorDetails errorDetails = new ErrorDetails(HttpStatus.CONFLICT);
		errorDetails.setMessage(ex.getMessage());
		
		return buildResponseEntity(errorDetails);
	}
	
	private ResponseEntity<Object> buildResponseEntity(ErrorDetails errorDetails) {
		return new ResponseEntity<>(errorDetails, errorDetails.getStatus());
	}
}
