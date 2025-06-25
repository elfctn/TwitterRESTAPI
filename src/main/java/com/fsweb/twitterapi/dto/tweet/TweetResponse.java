package com.fsweb.twitterapi.dto.tweet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fsweb.twitterapi.dto.user.UserResponse; // Tweet sahibinin bilgilerini dönmek için
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TweetResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponse user; // Tweet'i atan kullanıcının bilgileri (UserResponse DTO'su olarak)
    private UUID replyToTweetId; // Yanıt verilen tweet'in ID'si
    private UUID originalTweetId; // Retweet edilen tweet'in ID'si
    private Boolean isRetweet;
    // Not: Yorumlar, beğeniler, retweet sayıları gibi ek bilgileri daha sonra Controller veya Service katmanında doldurabiliriz.
    // Ya da bu DTO'yu daha da genişletebiliriz.
}