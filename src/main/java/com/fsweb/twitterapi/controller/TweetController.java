package com.fsweb.twitterapi.controller;

import com.fsweb.twitterapi.dto.tweet.TweetCreateRequest;
import com.fsweb.twitterapi.dto.tweet.TweetUpdateRequest;
import com.fsweb.twitterapi.dto.tweet.TweetResponse;
import com.fsweb.twitterapi.service.TweetService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/tweets")
public class TweetController {

    private static final Logger logger = LoggerFactory.getLogger(TweetController.class);


    private final TweetService tweetService;

    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    /**
     * Yeni bir tweet oluşturur.
     * Endpoint: POST /tweets
     * Erişim: Kimliği doğrulanmış kullanıcılar.
     *
     * @param request Yeni tweet bilgileri içeren TweetCreateRequest DTO
     * @return Oluşturulan tweet'in TweetResponse DTO'su ve HTTP 201 Created durumu
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TweetResponse> createTweet(@Valid @RequestBody TweetCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID currentUserId = userPrincipal.getId(); // Gerçek UUID'yi al

        logger.info("TweetController: createTweet request received. Current User ID: {}", currentUserId);

        TweetResponse newTweet = tweetService.createTweet(request, currentUserId);
        return new ResponseEntity<>(newTweet, HttpStatus.CREATED);
    }

    /**
     * Belirli bir tweet'i ID'sine göre getirir.
     * Endpoint: GET /tweets/{id}
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param id Getirilecek tweet'in UUID ID'si
     * @return Bulunan tweet'in TweetResponse DTO'su ve HTTP 200 OK durumu
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TweetResponse> getTweetById(@PathVariable UUID id) {
        TweetResponse tweet = tweetService.getTweetById(id);
        return ResponseEntity.ok(tweet);
    }

    /**
     * Bir kullanıcının tüm tweetlerini getirir.
     * Endpoint: GET /tweets/user/{userId}
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param userId Tweetleri getirilecek kullanıcının UUID ID'si
     * @return Kullanıcının tweetlerinin List<TweetResponse> DTO'su ve HTTP 200 OK durumu
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TweetResponse>> getTweetsByUserId(@PathVariable UUID userId) {
        List<TweetResponse> tweets = tweetService.getTweetsByUserId(userId);
        return ResponseEntity.ok(tweets);
    }

    /**
     * Belirli bir tweet'i ID'sine göre günceller.
     * Endpoint: PUT /tweets/{id}
     * Erişim: Sadece tweet sahibi güncelleyebilir.
     *
     * @param id Güncellenecek tweet'in UUID ID'si
     * @param request Tweet güncelleme bilgileri içeren TweetUpdateRequest DTO
     * @return Güncellenen tweet'in TweetResponse DTO'su ve HTTP 200 OK durumu
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TweetResponse> updateTweet(@PathVariable UUID id,
                                                     @Valid @RequestBody TweetUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID currentUserId = userPrincipal.getId();

        logger.info("TweetController: updateTweet request received. Tweet ID (from URL): {}, Current User ID (from JWT): {}", id, currentUserId);

        if (!currentUserId.equals(id)) {
            logger.warn("TweetController: Unauthorized update attempt. Current User ID: {} is not owner of tweet ID: {}", currentUserId, id);
            throw new UnauthorizedException("You are not authorized to update this tweet.");
        }

        TweetResponse updatedTweet = tweetService.updateTweet(id, request, currentUserId);
        return ResponseEntity.ok(updatedTweet);
    }

    /**
     * Belirli bir tweet'i ID'sine göre siler.
     * Endpoint: DELETE /tweets/{id}
     * Erişim: Sadece tweet sahibi silebilir.
     *
     * @param id Silinecek tweet'in UUID ID'si
     * @return HTTP 204 No Content durumu (başarılı silme)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTweet(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID currentUserId = userPrincipal.getId();

        logger.info("TweetController: deleteTweet request received. Tweet ID (from URL): {}, Current User ID (from JWT): {}", id, currentUserId);

        if (!currentUserId.equals(id)) {
            logger.warn("TweetController: Unauthorized delete attempt. Current User ID: {} is not owner of tweet ID: {}", currentUserId, id);
            throw new UnauthorizedException("You are not authorized to delete this tweet.");
        }

        tweetService.deleteTweet(id, currentUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}