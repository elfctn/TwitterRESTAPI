package com.fsweb.twitterapi.dto.user;

import jakarta.validation.constraints.Email; // E-posta validasyonu için
import jakarta.validation.constraints.NotBlank; // Boş olmama validasyonu için
import jakarta.validation.constraints.Size; // Uzunluk validasyonu için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için

@Data // Getter, Setter, equals(), hashCode(), toString() metodlarını otomatik oluşturur.
@NoArgsConstructor // Argümansız constructor oluşturur.
@AllArgsConstructor // Tüm alanları alan constructor oluşturur.
@Builder // Builder deseni oluşturur.
 public class UserRegisterRequest {

    @NotBlank(message = "Username cannot be empty") // Kullanıcı adı boş olamaz
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters") // Kullanıcı adı uzunluk kısıtlaması
    private String username;

    @NotBlank(message = "Email cannot be empty") // E-posta boş olamaz
    @Email(message = "Email should be valid") // E-posta formatı geçerli olmalı
    private String email;

    @NotBlank(message = "Password cannot be empty") // Şifre boş olamaz
    @Size(min = 6, message = "Password must be at least 6 characters long") // Şifre minimum uzunluk
    private String password;

    private String name; // İsim, opsiyonel
    private String surname; // Soyisim, opsiyonel
    private String bio; // Biyografi, opsiyonel
    private String profileImageUrl; // Profil fotoğrafı URL'si, opsiyonel
}