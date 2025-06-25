package com.fsweb.twitterapi.dto.user;

import jakarta.validation.constraints.NotBlank; // Boş olmama validasyonu için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRequest {

    @NotBlank(message = "Username or email cannot be empty") // Kullanıcı adı veya e-posta boş olamaz
    private String usernameOrEmail; // Kullanıcı adı veya e-posta ile giriş yapılabilir

    @NotBlank(message = "Password cannot be empty") // Şifre boş olamaz
    private String password;
}