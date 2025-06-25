package com.fsweb.twitterapi.service;

import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz (Tweet sahibini bulmak için)
import com.fsweb.twitterapi.repository.TweetRepository; // TweetRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadığında fırlatılacak istisna
import com.fsweb.twitterapi.exception.UnauthorizedException; // Yetkisiz işlem için istisna
import com.fsweb.twitterapi.dto.tweet.TweetCreateRequest; // Tweet oluşturma isteği DTO'su
import com.fsweb.twitterapi.dto.tweet.TweetUpdateRequest; // Tweet güncelleme isteği DTO'su
import com.fsweb.twitterapi.dto.tweet.TweetResponse; // Tweet yanıt DTO'su
import com.fsweb.twitterapi.dto.user.UserResponse; // UserResponse DTO'su (TweetResponse içinde kullanılacak)

import org.springframework.stereotype.Service; // Bu sınıfın bir Spring Service bileşeni olduğunu belirtmek için
import org.springframework.transaction.annotation.Transactional; // İşlemleri (transaction) yönetmek için

import java.util.List; // List tipini kullanmak için
import java.util.Optional;
import java.util.UUID; // UUID tipi için
import java.util.stream.Collectors; // Akış (Stream) API'si için

@Service // Bu anotasyon, Spring'e bu sınıfın bir servis bileşeni olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class TweetService {

    private final TweetRepository tweetRepository; // Tweet veri erişimi için
    private final UserRepository userRepository;   // Kullanıcı veri erişimi için

    // Constructor Injection: Spring, TweetService nesnesi oluşturulduğunda bu bağımlılıkları otomatik olarak sağlar.
    public TweetService(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    /**
     * Yeni bir tweet oluşturur.
     * Eğer bir yanıt veya retweet ise ilgili orijinal tweet'i bulur.
     *
     * @param request Yeni tweet bilgileri içeren DTO
     * @param userId Tweet'i oluşturan kullanıcının ID'si (güvenlik bağlamından gelir)
     * @return Oluşturulan tweet'in TweetResponse DTO'su
     */
    @Transactional
    public TweetResponse createTweet(TweetCreateRequest request, UUID userId) {
        // 1. Tweet'i atan kullanıcıyı bul (User entity'si gerekiyor)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Tweet tweet = Tweet.builder()
                .content(request.getContent())
                .user(user) // Tweet'i atan User objesini set et
                .isRetweet(request.getIsRetweet() != null ? request.getIsRetweet() : false) // isRetweet bilgisini al, null ise false varsay
                .build();

        // 2. Eğer bir yanıt (reply) tweet ise, yanıt verilen orijinal tweet'i bul
        if (request.getReplyToTweetId() != null) {
            Tweet replyToTweet = tweetRepository.findById(request.getReplyToTweetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reply to Tweet", "id", request.getReplyToTweetId()));
            tweet.setReplyToTweet(replyToTweet); // Yanıt verilen tweet objesini set et
        }

        // 3. Eğer bir retweet ise, orijinal tweet'i bul (Bu, Retweet entity'sinden farklı bir durum)
        if (request.getOriginalTweetId() != null) {
            Tweet originalTweet = tweetRepository.findById(request.getOriginalTweetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Original Tweet for Retweet", "id", request.getOriginalTweetId()));
            tweet.setOriginalTweet(originalTweet); // Orijinal tweet objesini set et
            // isRetweet true olmalı eğer originalTweetId varsa, güvenlik için burada tekrar kontrol edilebilir veya zorlanabilir.
            tweet.setIsRetweet(true);
        }

        // 4. Tweet'i kaydet
        Tweet savedTweet = tweetRepository.save(tweet);

        // 5. Kaydedilen Tweet entity'sinden TweetResponse DTO'su oluştur ve döndür
        return mapTweetToTweetResponse(savedTweet);
    }

    /**
     * Belirli bir ID'ye sahip tweet'i getirir.
     * Tweet bulunamazsa ResourceNotFoundException fırlatır.
     *
     * @param tweetId Getirilecek tweet'in UUID ID'si
     * @return Bulunan tweet'in TweetResponse DTO'su
     */
    public TweetResponse getTweetById(UUID tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet", "id", tweetId));
        return mapTweetToTweetResponse(tweet);
    }

    /**
     * Belirli bir kullanıcıya ait tüm tweetleri getirir.
     *
     * @param userId Tweetleri getirilmek istenen kullanıcının ID'si
     * @return Kullanıcının tweetlerinin List<TweetResponse> DTO'su
     */
    public List<TweetResponse> getTweetsByUserId(UUID userId) {
        // findByUserId metodu UserRepository değil, TweetRepository'deydi.
        List<Tweet> tweets = tweetRepository.findByUserId(userId);
        // Stream API kullanarak her bir Tweet entity'sini TweetResponse DTO'suna dönüştür.
        return tweets.stream()
                .map(this::mapTweetToTweetResponse) // Her Tweet objesi için mapTweetToTweetResponse metodunu çağır
                .collect(Collectors.toList()); // Sonuçları List olarak topla
    }

    /**
     * Belirli bir ID'ye sahip tweet'i günceller.
     * Sadece tweet'in sahibi güncelleyebilir.
     *
     * @param tweetId Güncellenecek tweet'in UUID ID'si
     * @param request Tweet güncelleme bilgileri içeren DTO
     * @param currentUserId İşlemi yapan mevcut kullanıcının ID'si (güvenlik bağlamından gelir)
     * @return Güncellenen tweet'in TweetResponse DTO'su
     */
    @Transactional
    public TweetResponse updateTweet(UUID tweetId, TweetUpdateRequest request, UUID currentUserId) {
        Tweet existingTweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet", "id", tweetId));

        // Yetkilendirme kontrolü: Sadece tweet sahibi güncelleyebilir
        if (!existingTweet.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not authorized to update this tweet.");
        }

        // Sadece içeriği güncelle
        // Not: DTO'da boş olmaması `@NotBlank` ile zaten kontrol edilir,
        // ancak yine de `Optional.ofNullable` ile null kontrolü bir alışkanlıktır.
        Optional.ofNullable(request.getContent())
                .filter(s -> !s.isEmpty()) // Boş string değilse güncelle
                .ifPresent(existingTweet::setContent);

        Tweet updatedTweet = tweetRepository.save(existingTweet);
        return mapTweetToTweetResponse(updatedTweet);
    }

    /**
     * Belirli bir ID'ye sahip tweet'i siler.
     * Sadece tweet'in sahibi silebilir.
     *
     * @param tweetId Silinecek tweet'in UUID ID'si
     * @param currentUserId İşlemi yapan mevcut kullanıcının ID'si (güvenlik bağlamından gelir)
     */
    @Transactional
    public void deleteTweet(UUID tweetId, UUID currentUserId) {
        Tweet existingTweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet", "id", tweetId));

        // Yetkilendirme kontrolü: Sadece tweet sahibi silebilir
        if (!existingTweet.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not authorized to delete this tweet.");
        }

        tweetRepository.delete(existingTweet); // Entity objesiyle silmek daha güvenli olabilir
    }

    // --- Yardımcı Metotlar ---

    /**
     * Tweet entity'sinden TweetResponse DTO'suna dönüşüm yapar.
     * User bilgisini UserResponse DTO'su olarak dahil eder.
     *
     * @param tweet Dönüştürülecek Tweet entity'si
     * @return TweetResponse DTO'su
     */
    private TweetResponse mapTweetToTweetResponse(Tweet tweet) {
        // User entity'sinden UserResponse DTO'suna dönüşüm
        UserResponse userResponse = UserResponse.builder()
                .id(tweet.getUser().getId())
                .username(tweet.getUser().getUsername())
                .email(tweet.getUser().getEmail())
                .name(tweet.getUser().getName())
                .surname(tweet.getUser().getSurname())
                .bio(tweet.getUser().getBio())
                .profileImageUrl(tweet.getUser().getProfileImageUrl())
                .createdAt(tweet.getUser().getCreatedAt())
                .updatedAt(tweet.getUser().getUpdatedAt())
                .build();

        return TweetResponse.builder()
                .id(tweet.getId())
                .content(tweet.getContent())
                .createdAt(tweet.getCreatedAt())
                .updatedAt(tweet.getUpdatedAt())
                .user(userResponse) // Kullanıcı bilgilerini ekle
                .replyToTweetId(tweet.getReplyToTweet() != null ? tweet.getReplyToTweet().getId() : null) // Yanıt verilen tweet ID'si
                .originalTweetId(tweet.getOriginalTweet() != null ? tweet.getOriginalTweet().getId() : null) // Orijinal tweet ID'si
                .isRetweet(tweet.getIsRetweet())
                .build();
    }
}