package com.fsweb.twitterapi.controller;

import com.fsweb.twitterapi.dto.user.UserResponse;
import com.fsweb.twitterapi.dto.user.UserUpdateRequest;
import com.fsweb.twitterapi.service.UserService;
import com.fsweb.twitterapi.exception.UnauthorizedException;
import com.fsweb.twitterapi.security.UserPrincipal;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Belirli bir kullanıcıyı ID'sine göre getirir.
     * Endpoint: GET /users/{id}
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param id Getirilecek kullanıcının UUID ID'si
     * @return Bulunan kullanıcının UserResponse DTO'su ve HTTP 200 OK durumu
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Mevcut kullanıcının profil bilgilerini günceller.
     * Endpoint: PUT /users/{id}
     * Erişim: Sadece kendi profilini güncelleyebilir.
     *
     * @param id Güncellenecek kullanıcının UUID ID'si (URL'den gelir)
     * @param request Kullanıcı güncelleme bilgileri içeren UserUpdateRequest DTO
     * @return Güncellenen kullanıcının UserResponse DTO'su ve HTTP 200 OK durumu
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID currentUserId = userPrincipal.getId(); // Gerçek UUID'yi al

        if (!currentUserId.equals(id)) {
            throw new UnauthorizedException("You are not authorized to update this user's profile.");
        }

        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Belirli bir kullanıcıyı siler.
     * Endpoint: DELETE /users/{id}
     * Erişim: Sadece kendi profilini silebilir. (Admin rolü de eklenebilir ileride)
     *
     * @param id Silinecek kullanıcının UUID ID'si
     * @return HTTP 204 No Content durumu (başarılı silme)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID currentUserId = userPrincipal.getId(); // Gerçek UUID'yi al

        if (!currentUserId.equals(id)) {
            throw new UnauthorizedException("You are not authorized to delete this user's profile.");
        }

        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}