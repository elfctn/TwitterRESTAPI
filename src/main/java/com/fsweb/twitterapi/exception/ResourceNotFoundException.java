package com.fsweb.twitterapi.exception;

import lombok.Data;
import org.springframework.http.HttpStatus; // HTTP durum kodları için (örn: 404 Not Found)
import org.springframework.web.bind.annotation.ResponseStatus; // İstisnayı HTTP durum koduna eşlemek için
@Data
@ResponseStatus(HttpStatus.NOT_FOUND) // Bu istisna fırlatıldığında HTTP 404 Not Found döneceğini belirtir.
public class ResourceNotFoundException extends RuntimeException {

    private String resourceName; // Hangi kaynak (entity) bulunamadı (örn: "User", "Tweet")
    private String fieldName;    // Hangi alanın değeri ile arama yapıldı (örn: "id", "username")
    private Object fieldValue;   // Alanın değeri (örn: "UUID_DEĞERİ", "test_user")

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        // Hata mesajını formatlar: "User not found with id : 'UUID_DEĞERİ'" gibi.
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }


}