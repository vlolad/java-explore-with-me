package ru.practicum.mainservice.exception;

public class ApiException extends RuntimeException {

    public ApiException(String msg) {
        super(msg);
    }
}
