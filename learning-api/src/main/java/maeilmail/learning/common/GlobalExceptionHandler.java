package maeilmail.learning.common;

import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());
        return ApiResponse.fail(ErrorCode.INVALID_REQUEST.name(), message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<?> handleMissingParam(MissingServletRequestParameterException e) {
        return ApiResponse.fail(ErrorCode.INVALID_REQUEST.name(), e.getParameterName() + " 파라미터가 필요합니다.");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<?> handleNotFound(ResourceNotFoundException e) {
        return ApiResponse.fail(ErrorCode.NOT_FOUND.name(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleUnexpected(Exception e) {
        log.error("예상치 못한 오류", e);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR);
    }
}
