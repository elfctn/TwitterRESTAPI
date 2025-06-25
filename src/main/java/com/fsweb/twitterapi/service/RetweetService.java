package com.fsweb.twitterapi.service;

import com.fsweb.twitterapi.entity.Retweet; // Retweet entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz (Orijinal tweet'i bulmak için)
import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz (Retweeti yapan kullanıcıyı bulmak için)
import com.fsweb.twitterapi.repository.RetweetRepository; // RetweetRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.repository.TweetRepository; // TweetRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadığında fırlatılacak istisna
import com.fsweb.twitterapi.exception.CustomValidationException; // İş kuralı validasyonları için istisna (örn: zaten retweet edilmiş)
import com.fsweb.twitterapi.dto.retweet.RetweetRequest; // Retweet işlemi için DTO (yalnızca originalTweetId içerir)

import org.springframework.stereotype.Service; // Bu sınıfın bir Spring Service bileşeni olduğunu belirtmek için
import org.springframework.transaction.annotation.Transactional; // İşlemleri (transaction) yönetmek için

import java.util.UUID; // UUID tipi için
import java.util.List; // List tipi için
import java.util.stream.Collectors; // Stream API için

@Service // Bu anotasyon, Spring'e bu sınıfın bir servis bileşeni olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class RetweetService {

    private final RetweetRepository retweetRepository;     // Retweet veri erişimi için
    private final TweetRepository tweetRepository;         // Tweet veri erişimi için
    private final UserRepository userRepository;           // Kullanıcı veri erişimi için

    // Constructor Injection
    public RetweetService(RetweetRepository retweetRepository, TweetRepository tweetRepository, UserRepository userRepository) {
        this.retweetRepository = retweetRepository;
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    /**
     * Bir kullanıcının belirli bir tweeti retweet etmesini sağlar.
     * Kullanıcı veya orijinal tweet bulunamazsa ResourceNotFoundException fırlatır.
     * Kullanıcı zaten tweeti retweet etmişse CustomValidationException fırlatır.
     *
     * @param originalTweetId Retweet edilecek orijinal tweet'in ID'si
     * @param userId Retweeti yapan kullanıcının ID'si (güvenlik bağlamından gelir)
     * @return Oluşturulan Retweet entity'si (Başarılı olursa)
     */
    @Transactional
    public Retweet retweetTweet(UUID originalTweetId, UUID userId) {
        // 1. Orijinal tweet'i ve kullanıcıyı bul
        Tweet originalTweet = tweetRepository.findById(originalTweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Original Tweet", "id", originalTweetId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 2. Kullanıcı bu tweeti zaten retweet etmiş mi kontrol et
        if (retweetRepository.existsByUserIdAndOriginalTweetId(userId, originalTweetId)) {
            throw new CustomValidationException("User with ID '" + userId + "' has already retweeted tweet with ID '" + originalTweetId + "'.");
        }

        // 3. Retweet entity'si oluştur
        Retweet retweet = Retweet.builder()
                .user(user)
                .originalTweet(originalTweet)
                .build();

        // 4. Retweet'i kaydet
        return retweetRepository.save(retweet);
    }

    /**
     * Bir kullanıcının belirli bir tweeti attığı retweeti kaldırır (unretweet).
     * Retweet bulunamazsa ResourceNotFoundException fırlatır.
     *
     * @param originalTweetId Retweeti kaldırılacak orijinal tweet'in ID'si
     * @param userId Retweeti kaldıran kullanıcının ID'si (güvenlik bağlamından gelir)
     */
    @Transactional
    public void unretweetTweet(UUID originalTweetId, UUID userId) {
        // 1. Retweet'in varlığını kontrol et ve bul
        Retweet existingRetweet = retweetRepository.findByUserIdAndOriginalTweetId(userId, originalTweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Retweet", "userId and originalTweetId", userId + " and " + originalTweetId));

        // 2. Retweet'i sil
        retweetRepository.delete(existingRetweet);
        // Alternatif olarak: retweetRepository.deleteByUserIdAndOriginalTweetId(userId, originalTweetId); de kullanılabilir.
    }

    /**
     * Belirli bir orijinal tweete ait tüm retweetleri getirir.
     *
     * @param originalTweetId Retweetleri getirilecek orijinal tweet'in ID'si
     * @return Retweetlerin List<Retweet> objesi
     */
    public List<Retweet> getRetweetsForOriginalTweet(UUID originalTweetId) {
        // Orijinal tweet'in varlığını kontrol et (opsiyonel, ancak iyi bir practice)
        if (!tweetRepository.existsById(originalTweetId)) {
            throw new ResourceNotFoundException("Original Tweet", "id", originalTweetId);
        }
        return retweetRepository.findByOriginalTweetId(originalTweetId);
    }

    /**
     * Bir kullanıcının belirli bir orijinal tweete retweet atıp atmadığını kontrol eder.
     *
     * @param originalTweetId Kontrol edilecek orijinal tweet'in ID'si
     * @param userId Kontrol edilecek kullanıcının ID'si
     * @return Kullanıcı tweeti retweet ettiyse true, etmediyse false
     */
    public boolean hasUserRetweetedTweet(UUID originalTweetId, UUID userId) {
        return retweetRepository.existsByUserIdAndOriginalTweetId(userId, originalTweetId);
    }
}