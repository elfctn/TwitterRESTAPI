package com.fsweb.twitterapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//Hem genel validasyon hataları
// hem de iş kuralı tabanlı (örn: kullanıcı adı zaten var) validasyon hataları için..


@ResponseStatus(HttpStatus.BAD_REQUEST) // Bu  fırlatıldığında HTTP 400 Bad Request döneceğini belirtir.
public class CustomValidationException extends RuntimeException {

    public CustomValidationException(String message) {
        super(message);
    }

    // İstersem daha spesifik alan ve değer ile de constructor ekleyebilirim:
    // public CustomValidationException(String fieldName, String fieldValue, String message) {
    //     super(String.format("%s: %s - %s", fieldName, fieldValue, message));
    // }
}