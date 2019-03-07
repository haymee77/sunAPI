package kr.co.sunpay.api.exception;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		List<String> errors = new ArrayList<String>();
		
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.add(error.getField() + ": " + error.getDefaultMessage());
		}
		
		ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST);
		errorDetails.setMessage(errors.toString());
		
		return buildResponseEntity(errorDetails);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
		ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST);
		errorDetails.setMessage(ex.getMessage());
		
		return buildResponseEntity(errorDetails);
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
	
	@ExceptionHandler(IllegalArgumentException.class)
	protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
		
		ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST);
		errorDetails.setMessage(ex.getMessage());
		
		return buildResponseEntity(errorDetails);
	}
	
	private ResponseEntity<Object> buildResponseEntity(ErrorDetails errorDetails) {
		return new ResponseEntity<>(errorDetails, errorDetails.getStatus());
	}
}
