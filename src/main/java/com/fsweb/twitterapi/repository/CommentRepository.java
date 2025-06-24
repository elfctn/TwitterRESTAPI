package com.fsweb.twitterapi.repository;

import com.fsweb.twitterapi.entity.Comment; // Comment entity'sini import ediyoruz
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import ediyoruz
import org.springframework.stereotype.Repository; // @Repository anotasyonu için

import java.util.List; // List tipini import ediyoruz
import java.util.UUID; // Comment, Tweet ve User ID tipi UUID olduğu için

@Repository // Bu arayüzün bir Spring Data Repository'si olduğunu belirtir.
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    // JpaRepository, <Entity Tipi, ID Tipi> parametrelerini alır.
    // Bu arayüz, Comment entity'si üzerinde temel CRUD metotlarını otomatik olarak sağlar.

    // Özel Sorgu Metotları:

    // Belirli bir tweete ait tüm yorumları getirme
    List<Comment> findByTweetId(UUID tweetId);

    // Belirli bir kullanıcıya ait tüm yorumları getirme
    List<Comment> findByUserId(UUID userId);

    // Belirli bir tweete ait ve belirli bir kullanıcı tarafından yapılan yorumu bulma (Silme veya güncelleme için kullanılabilir)
    // Spring Data JPA, Optional kullanarak sonucun var olup olmadığını daha güvenli yönetir.
    List<Comment> findByTweetIdAndUserId(UUID tweetId, UUID userId);
}