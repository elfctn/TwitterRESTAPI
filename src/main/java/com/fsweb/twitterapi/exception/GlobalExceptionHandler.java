package com.fsweb.twitterapi.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


//Bu sınıf, uygulamada fırlatılan özel istisnaları ve @Valid anotasyonuyla tetiklenen validasyon hatalarını merkezi olarak yakalar.


@ControllerAdvice // Bu anotasyon, bu sınıfın uygulamadaki tüm Controller'lar için global hata yönetimi sağlayacağını belirtir.
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // ResponseEntityExceptionHandler, Spring'in bazı yaygın Web hatalarını (örn. MethodArgumentNotValidException) ele almak için temel bir yapı sağlar.

    // ResourceNotFoundException'ı yakala
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException exception,
                                                                        WebRequest webRequest){
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND); // 404 Not Found döner
    }

    // CustomValidationException'ı yakala (Örn: Kullanıcı adı zaten var)
    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<ErrorDetails> handleCustomValidationException(CustomValidationException exception,
                                                                        WebRequest webRequest){
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // 400 Bad Request döner
    }

    // UnauthorizedException'ı yakala
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorDetails> handleUnauthorizedException(UnauthorizedException exception,
                                                                    WebRequest webRequest){
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED); // 401 Unauthorized döner
    }

    // Genel (tüm diğer) istisnaları yakala
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception,
                                                              WebRequest webRequest){
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error döner
    }

    // Validasyon hatalarını yakala (@Valid anotasyonları için, örn: DTO'lardaki @NotBlank hataları)
    @Override // ResponseEntityExceptionHandler sınıfından override ediyoruz
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>(); // Hata detaylarını (alan adı: hata mesajı) tutmak için map
        ex.getBindingResult().getAllErrors().forEach((error) ->{
            String fieldName = ((FieldError) error).getField(); // Hatanın olduğu alan adı
            String message = error.getDefaultMessage(); // Hata mesajı (DTO'daki message="...")
            errors.put(fieldName, message);
        });

        // Yanıt gövdesini (body) Map olarak döndürüyoruz, daha kullanıcı dostu bir validasyon hatası formatı.
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // 400 Bad Request döner
    }
}