package ru.practicum.mainservice.exception;

public class RestrictedException extends RuntimeException {

    public RestrictedException(String message) {
        super(message);
    }
}
