package com.app.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ApiError {
	private HttpStatus httpStatus;
	private List<String> errors;
	private LocalDateTime timestamp;
	private String path;
}
