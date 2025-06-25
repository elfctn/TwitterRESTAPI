package com.fsweb.twitterapi.dto.auth;

import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için

import java.util.UUID; // Kullanıcı ID'si için

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token; // JWT token
    private String type = "Bearer"; // Token tipi (genellikle "Bearer"dır)
    private UUID id; // Kullanıcı ID'si
    private String username; // Kullanıcı adı
    private String email; // Kullanıcı e-postası
    // Not: Roller gibi ek bilgiler de eklenebilir.
}