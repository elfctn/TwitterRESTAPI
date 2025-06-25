package com.fsweb.twitterapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED) // Bu istisna fırlatıldığında HTTP 401 Unauthorized döneceğini belirtir.
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("You are not authorized to perform this action.");
    }
}