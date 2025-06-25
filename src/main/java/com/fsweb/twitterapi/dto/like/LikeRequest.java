package com.fsweb.twitterapi.dto.like;

import jakarta.validation.constraints.NotNull; // Boş olamaz validasyonu için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için

import java.util.UUID; // Tweet ID tipi UUID olduğu için

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeRequest {

    @NotNull(message = "Tweet ID cannot be null") // Beğeni atılacak tweet'in ID'si boş olamaz
    private UUID tweetId;
    // Beğeniyi yapan kullanıcının ID'si genellikle güvenlik bağlamından (token'dan) alınır,
    // bu nedenle Request DTO'suna eklenmez.
}