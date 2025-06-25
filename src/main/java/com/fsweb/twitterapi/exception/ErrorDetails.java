package com.fsweb.twitterapi.exception; // Exception paketi altında olacak

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//GlobalExceptionHandler'ın döneceği
// standart hata yanıt formatı için bu DTO'ya ihtiyacım var.
//Bu aslında bir DTO olmuş olur

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String details;
}