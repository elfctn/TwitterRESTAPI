package com.fsweb.twitterapi.repository;

import com.fsweb.twitterapi.entity.Like; // Like entity'sini import ediyoruz
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import ediyoruz
import org.springframework.stereotype.Repository; // @Repository anotasyonu için

import java.util.List; // List tipini import ediyoruz
import java.util.Optional; // Optional tipini import ediyoruz
import java.util.UUID; // Like, User ve Tweet ID tipi UUID olduğu için

@Repository // Bu arayüzün bir Spring Data Repository'si olduğunu belirtir.
public interface LikeRepository extends JpaRepository<Like, UUID> {
    // JpaRepository, <Entity Tipi, ID Tipi> parametrelerini alır.
    // Biz Like entity'sini UUID ID ile modellediğimiz için burası UUID.

    // Özel Sorgu Metotları:

    // Belirli bir kullanıcıya ait tüm beğenileri getirme
    List<Like> findByUserId(UUID userId);

    // Belirli bir tweete ait tüm beğenileri getirme
    List<Like> findByTweetId(UUID tweetId);

    // Belirli bir kullanıcının belirli bir tweete like atıp atmadığını kontrol etme (Optional ile)
    Optional<Like> findByUserIdAndTweetId(UUID userId, UUID tweetId);

    // Belirli bir kullanıcının belirli bir tweete like atıp atmadığını kontrol etme (boolean ile daha hızlı)
    boolean existsByUserIdAndTweetId(UUID userId, UUID tweetId);

    // Belirli bir kullanıcının belirli bir tweete attığı like'ı silme (silme operasyonlarında kullanılabilir)
    void deleteByUserIdAndTweetId(UUID userId, UUID tweetId);
}


//NOT:Optional kullanmak, sonuç bulunmadığında null pointer exception almamızı engeller.

//Optional<T>
//Benzersiz ID'ler, Unique alanlar
//At most one (0 or 1)
//NullPointerException riskini kaldırır
//BoşDurum: Optional.empty()


//Optional<T>
//Koleksiyonler, Filtrelemeler
//Zero or more (0..N)
//NullPointerException riskini hiç yok
//BoşDurum: Collections.emptyList()