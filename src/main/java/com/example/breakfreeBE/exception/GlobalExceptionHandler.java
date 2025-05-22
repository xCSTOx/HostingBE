package com.example.breakfreeBE.exception;

import com.example.breakfreeBE.common.BaseResponse;
import com.example.breakfreeBE.common.MetaResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        BaseResponse<Object> response = new BaseResponse<>(
                new MetaResponse(false, ex.getMessage()),
                null
        );
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleOtherExceptions(Exception ex) {
        BaseResponse<Object> response = new BaseResponse<>(
                new MetaResponse(false, "Internal Server Error"),
                null
        );
        return ResponseEntity.status(500).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        MetaResponse meta = new MetaResponse(false, ex.getMessage());
        BaseResponse<Object> response = new BaseResponse<>(meta, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleEntityNotFound(EntityNotFoundException ex) {
        BaseResponse<Object> response = new BaseResponse<>(
                new MetaResponse(false, ex.getMessage()),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

}
