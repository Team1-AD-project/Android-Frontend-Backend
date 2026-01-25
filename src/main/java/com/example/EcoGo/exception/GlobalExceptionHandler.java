package com.example.EcoGo.exception;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseMessage<?> handlerBusinessException(BusinessException e) {
        return new ResponseMessage<>(e.getCode(), e.getMessage(), null);
    }

    // Handle parameter validation exceptions (ConstraintViolationException)
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseBody
    public ResponseMessage<?> handlerConstraintViolationException(
            jakarta.validation.ConstraintViolationException e) {
        // Extract the first error message
        String message = e.getConstraintViolations().stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Parameter validation error");
        return new ResponseMessage<>(ErrorCode.PARAM_ERROR.getCode(), message, null);
    }

    // Handle object parameter validation exceptions (MethodArgumentNotValidException)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseMessage<?> handlerMethodArgumentNotValidException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(org.springframework.validation.ObjectError::getDefaultMessage)
                .findFirst()
                .orElse("Parameter validation error");
        return new ResponseMessage<>(ErrorCode.PARAM_ERROR.getCode(), message, null);
    }

    // Handle system exceptions (e.g., NullPointerException, DB connection failure)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseMessage<?> handlerSystemException(Exception e) {
        log.error("System exception", e); // Log detailed error
        return new ResponseMessage<>(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage(), null);
    }
}
