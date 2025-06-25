package com.fsweb.twitterapi.service;

import com.fsweb.twitterapi.entity.Like; // Like entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz (Beğenilen tweet'i bulmak için)
import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz (Beğeniyi yapan kullanıcıyı bulmak için)
import com.fsweb.twitterapi.repository.LikeRepository; // LikeRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.repository.TweetRepository; // TweetRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadığında fırlatılacak istisna
import com.fsweb.twitterapi.exception.CustomValidationException; // İş kuralı validasyonları için istisna (örn: zaten beğenilmiş)
import com.fsweb.twitterapi.dto.like.LikeRequest; // Like işlemi için DTO (yalnızca tweetId içerir)

import org.springframework.stereotype.Service; // Bu sınıfın bir Spring Service bileşeni olduğunu belirtmek için
import org.springframework.transaction.annotation.Transactional; // İşlemleri (transaction) yönetmek için

import java.util.UUID; // UUID tipi için
import java.util.List; // List tipi için
import java.util.stream.Collectors; // Stream API için

@Service // Bu anotasyon, Spring'e bu sınıfın bir servis bileşeni olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class LikeService {

    private final LikeRepository likeRepository;   // Beğeni veri erişimi için
    private final TweetRepository tweetRepository; // Tweet veri erişimi için
    private final UserRepository userRepository;   // Kullanıcı veri erişimi için

    // Constructor Injection
    public LikeService(LikeRepository likeRepository, TweetRepository tweetRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    /**
     * Bir kullanıcının belirli bir tweete beğeni (like) atmasını sağlar.
     * Kullanıcı veya tweet bulunamazsa ResourceNotFoundException fırlatır.
     * Kullanıcı zaten tweeti beğenmişse CustomValidationException fırlatır.
     *
     * @param tweetId Beğenilecek tweet'in ID'si
     * @param userId Beğeniyi yapan kullanıcının ID'si (güvenlik bağlamından gelir)
     * @return Oluşturulan Like entity'si (Başarılı olursa)
     */
    @Transactional
    public Like likeTweet(UUID tweetId, UUID userId) {
        // 1. Tweet'i ve kullanıcıyı bul
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet", "id", tweetId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 2. Kullanıcı bu tweeti zaten beğenmiş mi kontrol et
        if (likeRepository.existsByUserIdAndTweetId(userId, tweetId)) {
            throw new CustomValidationException("User with ID '" + userId + "' has already liked tweet with ID '" + tweetId + "'.");
        }

        // 3. Like entity'si oluştur
        Like like = Like.builder()
                .user(user)
                .tweet(tweet)
                .build();

        // 4. Like'ı kaydet
        return likeRepository.save(like);
    }

    /**
     * Bir kullanıcının belirli bir tweete attığı beğeniyi (dislike) kaldırır.
     * Beğeni bulunamazsa ResourceNotFoundException fırlatır.
     *
     * @param tweetId Beğenisi kaldırılacak tweet'in ID'si
     * @param userId Beğeniyi kaldıran kullanıcının ID'si (güvenlik bağlamından gelir)
     */
    @Transactional
    public void dislikeTweet(UUID tweetId, UUID userId) {
        // 1. Like'ın varlığını kontrol et ve bul
        Like existingLike = likeRepository.findByUserIdAndTweetId(userId, tweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Like", "userId and tweetId", userId + " and " + tweetId));

        // 2. Beğeniyi sil
        likeRepository.delete(existingLike);
        // Alternatif olarak: likeRepository.deleteByUserIdAndTweetId(userId, tweetId); de kullanılabilir.
    }

    /**
     * Belirli bir tweete ait tüm beğenileri getirir.
     *
     * @param tweetId Beğenileri getirilecek tweet'in ID'si
     * @return Beğenilerin List<Like> objesi
     */
    public List<Like> getLikesForTweet(UUID tweetId) {
        // Tweet'in varlığını kontrol et (opsiyonel, ancak iyi bir practice)
        if (!tweetRepository.existsById(tweetId)) {
            throw new ResourceNotFoundException("Tweet", "id", tweetId);
        }
        return likeRepository.findByTweetId(tweetId);
    }

    /**
     * Bir kullanıcının belirli bir tweete like atıp atmadığını kontrol eder.
     *
     * @param tweetId Kontrol edilecek tweet'in ID'si
     * @param userId Kontrol edilecek kullanıcının ID'si
     * @return Kullanıcı tweeti beğendiyse true, beğenmediyse false
     */
    public boolean hasUserLikedTweet(UUID tweetId, UUID userId) {
        return likeRepository.existsByUserIdAndTweetId(userId, tweetId);
    }
}