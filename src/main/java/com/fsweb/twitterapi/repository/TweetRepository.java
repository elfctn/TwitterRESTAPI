package com.fsweb.twitterapi.repository;

import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import ediyoruz
import org.springframework.stereotype.Repository; // @Repository anotasyonu için

import java.util.List; // List tipini import ediyoruz
import java.util.UUID; // Tweet ve User ID tipi UUID olduğu için

@Repository // Bu arayüzün bir Spring Data Repository'si olduğunu belirtir.
public interface TweetRepository extends JpaRepository<Tweet, UUID> {
    // JpaRepository, <Entity Tipi, ID Tipi> parametrelerini alır.
    // Bu arayüz, Tweet entity'si üzerinde temel CRUD metotlarını otomatik olarak sağlar.

    // Özel Sorgu Metotları:

    // Bir kullanıcının tüm tweetlerini getirme (findByUserId gereksinimi için)
    // Spring Data JPA, ilişkili entity'nin ID'sine göre arama yapmak için JoinColumn adını kullanabiliriz.
    // Veya daha okunabilir olması için `findByUser_Id` şeklinde de yazılabilir.
    List<Tweet> findByUserId(UUID userId);

    // İçeriğe göre tweet arama (opsiyonel ama sık kullanılan bir sorguymuş)
    // `Containing` anahtar kelimesi, LIKE operatörünü kullanarak alt string araması yapar.
    // `IgnoreCase` ise büyük/küçük harf duyarsız arama yapar.
    List<Tweet> findByContentContainingIgnoreCase(String content);

    // Belirli bir kullanıcıya ait ve içeriği belirli bir kelimeyi içeren tweetleri bulma
    List<Tweet> findByUserIdAndContentContainingIgnoreCase(UUID userId, String content);
}