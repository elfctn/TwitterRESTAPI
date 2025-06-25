package com.fsweb.twitterapi.dto.user;

import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için

import java.time.LocalDateTime; // Tarih ve saat tipi için
import java.util.UUID; // UUID tipi için

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
 public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String name;
    private String surname;
    private String bio;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // TODO: Şifre gibi hassas bilgileri asla Response DTO'sunda göndermeyeceğim
}