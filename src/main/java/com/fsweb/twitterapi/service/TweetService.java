package com.fsweb.twitterapi.service;

import com.fsweb.twitterapi.entity.Tweet;
import com.fsweb.twitterapi.entity.User;
import com.fsweb.twitterapi.repository.TweetRepository;
import com.fsweb.twitterapi.repository.UserRepository;
import com.fsweb.twitterapi.exception.ResourceNotFoundException;
import com.fsweb.twitterapi.exception.UnauthorizedException;
import com.fsweb.twitterapi.dto.tweet.TweetCreateRequest;
import com.fsweb.twitterapi.dto.tweet.TweetUpdateRequest;
import com.fsweb.twitterapi.dto.tweet.TweetResponse;
import com.fsweb.twitterapi.dto.user.UserResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

// Logger için import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TweetService {

    private static final Logger logger = LoggerFactory.getLogger(TweetService.class); // Logger eklendi

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Tweet tweet = Tweet.builder()
                .content(request.getContent())
                .user(user)
                .isRetweet(request.getIsRetweet() != null ? request.getIsRetweet() : false)
                .build();

        if (request.getReplyToTweetId() != null) {
            Tweet replyToTweet = tweetRepository.findById(request.getReplyToTweetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reply to Tweet", "id", request.getReplyToTweetId()));
            tweet.setReplyToTweet(replyToTweet);
        }

        if (request.getOriginalTweetId() != null) {
            Tweet originalTweet = tweetRepository.findById(request.getOriginalTweetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Original Tweet for Retweet", "id", request.getOriginalTweetId()));
            tweet.setOriginalTweet(originalTweet);
            tweet.setIsRetweet(true);
        }

        Tweet savedTweet = tweetRepository.save(tweet);
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
        List<Tweet> tweets = tweetRepository.findByUserId(userId);
        return tweets.stream()
                .map(this::mapTweetToTweetResponse)
                .collect(Collectors.toList());
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

        logger.info("Attempting to update tweet. Tweet ID: {}, Current User ID: {}", tweetId, currentUserId); // Log eklendi
        logger.info("Tweet Owner ID (from DB): {}", existingTweet.getUser().getId()); // Log eklendi

        if (!existingTweet.getUser().getId().equals(currentUserId)) {
            logger.warn("Unauthorized attempt to update tweet. Current User ID: {} is not owner (Tweet Owner: {}).", currentUserId, existingTweet.getUser().getId()); // Log eklendi
            throw new UnauthorizedException("You are not authorized to update this tweet.");
        }

        Optional.ofNullable(request.getContent())
                .filter(s -> !s.isEmpty())
                .ifPresent(existingTweet::setContent);

        Tweet updatedTweet = tweetRepository.save(existingTweet);
        logger.info("Tweet with ID {} successfully updated by user {}.", tweetId, currentUserId); // Log eklendi
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

        logger.info("Attempting to delete tweet. Tweet ID: {}, Current User ID: {}", tweetId, currentUserId); // Log eklendi
        logger.info("Tweet Owner ID (from DB): {}", existingTweet.getUser().getId()); // Log eklendi

        if (!existingTweet.getUser().getId().equals(currentUserId)) {
            logger.warn("Unauthorized attempt to delete tweet. Current User ID: {} is not owner (Tweet Owner: {}).", currentUserId, existingTweet.getUser().getId()); // Log eklendi
            throw new UnauthorizedException("You are not authorized to delete this tweet.");
        }

        tweetRepository.delete(existingTweet);
        logger.info("Tweet with ID {} successfully deleted by user {}.", tweetId, currentUserId); // Log eklendi
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
                .user(userResponse)
                .replyToTweetId(tweet.getReplyToTweet() != null ? tweet.getReplyToTweet().getId() : null)
                .originalTweetId(tweet.getOriginalTweet() != null ? tweet.getOriginalTweet().getId() : null)
                .isRetweet(tweet.getIsRetweet())
                .build();
    }
}