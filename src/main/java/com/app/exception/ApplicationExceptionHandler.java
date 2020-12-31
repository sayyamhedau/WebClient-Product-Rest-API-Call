package com.app.exception;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.app.entity.ApiError;

@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = RuntimeException.class)
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		BindingResult bindingResult = ex.getBindingResult();
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		List<String> errors = fieldErrors.stream().map(error -> error.getField() + " : " + error.getDefaultMessage())
				.collect(Collectors.toList());

		ApiError apiError = new ApiError();
		apiError.setErrors(errors);
		apiError.setHttpStatus(HttpStatus.BAD_REQUEST);
		apiError.setPath(request.getDescription(false));
		apiError.setTimestamp(LocalDateTime.now(ZoneId.systemDefault()));

		return ResponseEntity.status(apiError.getHttpStatus()).headers(new HttpHeaders()).body(apiError);
	}

	@ExceptionHandler(value = { ConstraintViolationException.class })
	public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex,
			ServletWebRequest request) {

		Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
		List<String> errors = constraintViolations.stream()
				.map(error -> error.getRootBeanClass() + " : " + error.getPropertyPath() + " : " + error.getMessage())
				.collect(Collectors.toList());

		ApiError apiError = new ApiError();
		apiError.setErrors(errors);
		apiError.setHttpStatus(HttpStatus.BAD_REQUEST);
		apiError.setPath(request.getRequest().getRequestURI());
		apiError.setTimestamp(LocalDateTime.now(ZoneId.systemDefault()));

		return ResponseEntity.status(apiError.getHttpStatus()).headers(new HttpHeaders()).body(apiError);
	}

	@ExceptionHandler(value = ProductNotFoundException.class)
	public ResponseEntity<Object> handleProductNotFoundExceptionHandler(RuntimeException ex,
			ServletWebRequest request) {
		ApiError apiError = new ApiError();
		apiError.setHttpStatus(HttpStatus.NOT_FOUND);
		apiError.setErrors(Arrays.asList(ex.getMessage()));
		apiError.setPath(request.getRequest().getRequestURI());
		apiError.setTimestamp(LocalDateTime.now(ZoneId.systemDefault()));

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
	}
}
