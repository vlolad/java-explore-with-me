package ru.practicum.mainservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.mainservice.exception.BadRequestException;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.exception.RestrictedException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    static {
        Locale.setDefault(new Locale("en"));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleRestrictedException(final RestrictedException e) {
        log.error("RestrictedException: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("FORBIDDEN");
        response.setMessage(e.getMessage());
        response.setReason("For the requested operation the conditions are not met.");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequestException(final BadRequestException e) {
        log.error("BadRequestException: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("BAD REQUEST");
        response.setMessage(e.getMessage());
        response.setReason("For the requested operation the conditions are not met.");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFoundException(final NotFoundException e) {
        log.error("NotFoundException: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("NOT FOUND");
        response.setMessage(e.getMessage());
        response.setReason("The required object was not found.");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIllegalArgumentException(final IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("BAD REQUEST");
        response.setMessage(e.getMessage());
        response.setReason("For the requested operation the conditions are not met.");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleConstraintViolationException(final ConstraintViolationException e) {
        log.error("ConstraintViolationException: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("BAD REQUEST");
        response.setMessage(e.getMessage());
        response.setReason("For the requested operation the conditions are not met.");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("BAD REQUEST");
        response.setMessage(e.getMessage());
        response.setReason("For the requested operation the conditions are not met.");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("BAD REQUEST");
        response.setMessage(e.getMessage());
        response.setReason("For the requested operation the conditions are not met.");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("CONFLICT");
        response.setMessage(e.getMessage());
        response.setReason("For the requested operation the conditions are not met.");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleThrowable(final Throwable e) {
        log.error("Throwable: {}", e.getMessage());
        ApiErrorResponse response = new ApiErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("INTERNAL SERVER ERROR");
        response.setMessage(e.getMessage());
        response.setReason("Error occurred.");
        return response;
    }
}
