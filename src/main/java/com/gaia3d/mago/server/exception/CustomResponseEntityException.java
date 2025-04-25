package com.gaia3d.mago.server.exception;

import com.gaia3d.mago.server.domain.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
public class CustomResponseEntityException extends ResponseEntityExceptionHandler {
    @ExceptionHandler({MemberNotFoundException.class, TilesNotFoundException.class})
    public final ResponseEntity<ExceptionResponse> handleNotFoundException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler({MemberUnauthorizedException.class, MemberAlreadyExistException.class})
    public final ResponseEntity<ExceptionResponse> handleUnauthorizedException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler({TilesInvalidException.class})
    public final ResponseEntity<ExceptionResponse> handleInvalidException(Exception ex, WebRequest request) {
        return ResponseEntity.internalServerError().body(new ExceptionResponse(ex.getMessage(), request.getDescription(false)));
    }
}
