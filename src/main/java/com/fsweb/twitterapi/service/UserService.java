package com.fsweb.twitterapi.service;

import com.fsweb.twitterapi.entity.User;
import com.fsweb.twitterapi.repository.UserRepository;
import com.fsweb.twitterapi.exception.ResourceNotFoundException;
import com.fsweb.twitterapi.exception.CustomValidationException;
import com.fsweb.twitterapi.dto.user.UserRegisterRequest;
import com.fsweb.twitterapi.dto.user.UserResponse;
import com.fsweb.twitterapi.dto.user.UserUpdateRequest;
import com.fsweb.twitterapi.dto.user.UserLoginRequest;
import com.fsweb.twitterapi.dto.auth.JwtResponse;
import com.fsweb.twitterapi.security.jwt.JwtProvider;
import com.fsweb.twitterapi.security.UserPrincipal;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomValidationException("Username '" + request.getUsername() + "' already exists.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomValidationException("Email '" + request.getEmail() + "' already exists.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .surname(request.getSurname())
                .bio(request.getBio())
                .profileImageUrl(request.getProfileImageUrl())
                .build();

        User savedUser = userRepository.save(user);

        return mapUserToUserResponse(savedUser);
    }

    public JwtResponse loginUser(UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);

        // KESİNLİKLE DÜZELTME: JWT'den kullanıcı ID'sini alıyoruz
        String userIdStringFromJwt = jwtProvider.getUserIdFromJwtToken(jwt); // Yeni metot adı
        UUID userId = UUID.fromString(userIdStringFromJwt); // JWT'den gelen ID string'ini UUID'ye çevir

        // Kullanıcının detaylarını ID ile getiriyoruz
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapUserToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Optional.ofNullable(request.getName()).filter(s -> !s.isEmpty()).ifPresent(existingUser::setName);
        Optional.ofNullable(request.getSurname()).filter(s -> !s.isEmpty()).ifPresent(existingUser::setSurname);
        Optional.ofNullable(request.getBio()).filter(s -> !s.isEmpty()).ifPresent(existingUser::setBio);
        Optional.ofNullable(request.getProfileImageUrl()).filter(s -> !s.isEmpty()).ifPresent(existingUser::setProfileImageUrl);

        User updatedUser = userRepository.save(existingUser);
        return mapUserToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapUserToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}