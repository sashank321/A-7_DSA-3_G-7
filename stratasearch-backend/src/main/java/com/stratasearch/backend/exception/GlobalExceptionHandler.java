package com.stratasearch.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import com.stratasearch.backend.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> HandleApi(ApiException Ex, WebRequest Req) {
        ErrorResponse Body = new ErrorResponse(
                System.currentTimeMillis(),
                Ex.GetStatus(),
                Ex.GetStatus() == 400 ? "Bad Request" : (Ex.GetStatus() == 404 ? "Not Found" : "Error"),
                Ex.getMessage(),
                Req.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(Ex.GetStatus()).body(Body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> HandleGeneric(Exception Ex, WebRequest Req) {
        ErrorResponse Body = new ErrorResponse(
                System.currentTimeMillis(),
                500,
                "Internal Server Error",
                Ex.getMessage(),
                Req.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(500).body(Body);
    }
}
