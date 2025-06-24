package com.fsweb.twitterapi.repository;

import com.fsweb.twitterapi.entity.Retweet; // Retweet entity'sini import ediyoruz
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import ediyoruz
import org.springframework.stereotype.Repository; // @Repository anotasyonu için

import java.util.List; // List tipini import ediyoruz
import java.util.Optional; // Optional tipini import ediyoruz
import java.util.UUID; // Retweet, User ve Tweet ID tipi UUID olduğu için

@Repository // Bu arayüzün bir Spring Data Repository'si olduğunu belirtir.
public interface RetweetRepository extends JpaRepository<Retweet, UUID> {
    // JpaRepository, <Entity Tipi, ID Tipi> parametrelerini alır.
    // Biz Retweet entity'sini UUID ID ile modellediğimiz için burası UUID.

    // Özel Sorgu Metotları:

    // Belirli bir kullanıcıya ait tüm retweetleri getirme
    List<Retweet> findByUserId(UUID userId);

    // Belirli bir orijinal tweete ait tüm retweetleri getirme
    // originalTweet alanı Tweet entity'sinde olduğu için originalTweet.id kullanılır.
    List<Retweet> findByOriginalTweetId(UUID originalTweetId);

    // Belirli bir kullanıcının belirli bir orijinal tweete retweet atıp atmadığını kontrol etme
    Optional<Retweet> findByUserIdAndOriginalTweetId(UUID userId, UUID originalTweetId);

    // Belirli bir kullanıcının belirli bir orijinal tweete retweet atıp atmadığını kontrol etme (boolean ile daha hızlı)
    boolean existsByUserIdAndOriginalTweetId(UUID userId, UUID originalTweetId);

    // Belirli bir kullanıcının belirli bir orijinal tweete attığı retweeti silme
    void deleteByUserIdAndOriginalTweetId(UUID userId, UUID originalTweetId);
}