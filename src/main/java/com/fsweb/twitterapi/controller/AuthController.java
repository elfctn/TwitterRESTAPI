package com.fsweb.twitterapi.controller;

import com.fsweb.twitterapi.dto.auth.JwtResponse; // JWT yanıt DTO'su
import com.fsweb.twitterapi.dto.user.UserLoginRequest; // Kullanıcı giriş isteği DTO'su
import com.fsweb.twitterapi.dto.user.UserRegisterRequest; // Kullanıcı kayıt isteği DTO'su
import com.fsweb.twitterapi.dto.user.UserResponse; // Kullanıcı yanıt DTO'su
import com.fsweb.twitterapi.service.UserService; // UserService'i enjekte edeceğiz

import jakarta.validation.Valid; // Validasyon için (DTO'lardaki @NotBlank, @Size vb. kontrolü)
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.web.bind.annotation.PostMapping; // POST isteklerini karşılamak için
import org.springframework.web.bind.annotation.RequestBody; // HTTP isteğinin body'sini Java objesine dönüştürmek için
import org.springframework.web.bind.annotation.RequestMapping; // İstekleri belirli bir URL yoluna eşlemek için
import org.springframework.web.bind.annotation.RestController; // RESTful Controller olduğunu belirtmek için

@RestController // Bu sınıfın bir REST Controller olduğunu ve metodların doğrudan HTTP yanıtı döneceğini belirtir.
@RequestMapping("/auth") // Bu Controller'daki tüm endpoint'lerin "/auth" yoluyla başlayacağını belirtir.
public class AuthController {

    private final UserService userService; // UserService'i enjekte ediyoruz

    // Constructor Injection
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Yeni bir kullanıcı kaydeder (register).
     * Endpoint: POST /auth/register
     *
     * @param request Kullanıcı kayıt bilgileri içeren UserRegisterRequest DTO
     * @return Kaydedilen kullanıcının UserResponse DTO'su ve HTTP 201 Created durumu
     */
    @PostMapping("/register") // POST isteği için "/register" yolunu eşler
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        // @Valid: Gelen DTO'nun (request) içindeki validasyon kurallarını (@NotBlank, @Size vb.) kontrol eder.
        // @RequestBody: HTTP isteğinin JSON body'sini UserRegisterRequest objesine dönüştürür.

        UserResponse newUser = userService.registerUser(request); // UserService'i çağırarak kullanıcıyı kaydet
        return new ResponseEntity<>(newUser, HttpStatus.CREATED); // HTTP 201 Created durum kodu ile yanıt dön
    }

    /**
     * Kullanıcının giriş yapmasını sağlar (login) ve bir JWT token döndürür.
     * Endpoint: POST /auth/login
     *
     * @param request Kullanıcı giriş bilgileri içeren UserLoginRequest DTO
     * @return JWT token ve kullanıcı detayları içeren JwtResponse DTO'su ve HTTP 200 OK durumu
     */
    @PostMapping("/login") // POST isteği için "/login" yolunu eşler
    public ResponseEntity<JwtResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
        JwtResponse jwtResponse = userService.loginUser(request); // UserService'i çağırarak giriş yap ve JWT al
        return ResponseEntity.ok(jwtResponse); // HTTP 200 OK durum kodu ile yanıt dön
    }
}