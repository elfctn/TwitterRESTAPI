package com.fsweb.twitterapi;

import com.fsweb.twitterapi.entity.Like; // Like entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz
import com.fsweb.twitterapi.exception.CustomValidationException; // Özel validasyon istisnamızı import ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadı istisnamızı import ediyoruz
import com.fsweb.twitterapi.repository.LikeRepository; // LikeRepository'yi mock'layacağız
import com.fsweb.twitterapi.repository.TweetRepository; // TweetRepository'yi mock'layacağız
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi mock'layacağız
import com.fsweb.twitterapi.service.LikeService; // Test edeceğimiz Service sınıfı
import com.fsweb.twitterapi.dto.like.LikeRequest; // Like DTO'sumuzu import ediyoruz

import org.junit.jupiter.api.BeforeEach; // Her test metodundan önce çalışacak kurulum için
import org.junit.jupiter.api.DisplayName; // Test metodlarına daha okunabilir isimler vermek için
import org.junit.jupiter.api.Test; // Bir test metodu olduğunu belirtmek için
import org.junit.jupiter.api.extension.ExtendWith; // Mockito'yu JUnit 5 ile entegre etmek için
import org.mockito.InjectMocks; // Mock'lanmış bağımlılıkları enjekte etmek için
import org.mockito.Mock; // Sahte (mock) bağımlılıklar oluşturmak için
import org.mockito.junit.jupiter.MockitoExtension; // Mockito uzantısı için

import java.time.LocalDateTime; // Tarih ve saat tipi için
import java.util.List; // List tipi için
import java.util.Optional; // Optional kullanmak için
import java.util.UUID; // UUID tipi için

import static org.junit.jupiter.api.Assertions.*; // JUnit assert metotları için
import static org.mockito.ArgumentMatchers.any; // Herhangi bir argümanı eşleştirmek için
import static org.mockito.Mockito.*; // Mockito metotları için

@ExtendWith(MockitoExtension.class) // JUnit 5'i Mockito ile entegre eder.
@DisplayName("LikeService Unit Tests") // Test sınıfına okunabilir bir isim verir
public class LikeServiceTest {

    @Mock // LikeRepository'nin sahte bir versiyonunu oluşturur.
    private LikeRepository likeRepository;

    @Mock // TweetRepository'nin sahte bir versiyonunu oluşturur.
    private TweetRepository tweetRepository;

    @Mock // UserRepository'nin sahte bir versiyonunu oluşturur.
    private UserRepository userRepository;

    @InjectMocks // Test edilecek gerçek LikeService nesnesini oluşturur ve bağımlılıkları enjekte eder.
    private LikeService likeService;

    // Testlerde kullanılacak örnek veri
    private User testUser;
    private Tweet testTweet;
    private Like testLike;
    private UUID testUserId;
    private UUID testTweetId;
    private UUID testLikeId;

    @BeforeEach // Her test metodundan önce çalışacak kurulum metodu.
    void setUp() {
        testUserId = UUID.randomUUID();
        testTweetId = UUID.randomUUID();
        testLikeId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .username("likeruser")
                .email("liker@example.com")
                .password("encodedpass")
                .createdAt(LocalDateTime.now())
                .build();

        testTweet = Tweet.builder()
                .id(testTweetId)
                .content("Tweet to be liked")
                .user(User.builder().id(UUID.randomUUID()).username("tweetowner").build()) // Farklı bir tweet sahibi
                .createdAt(LocalDateTime.now())
                .build();

        testLike = Like.builder()
                .id(testLikeId)
                .user(testUser)
                .tweet(testTweet)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- likeTweet Metodu Testleri ---

    @Test
    @DisplayName("should like a tweet successfully")
    void shouldLikeTweetSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));
        when(likeRepository.existsByUserIdAndTweetId(testUserId, testTweetId)).thenReturn(false); // Henüz beğenilmemiş
        when(likeRepository.save(any(Like.class))).thenReturn(testLike);

        Like result = likeService.likeTweet(testTweetId, testUserId);

        assertNotNull(result);
        assertEquals(testLikeId, result.getId());
        assertEquals(testUserId, result.getUser().getId());
        assertEquals(testTweetId, result.getTweet().getId());

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(likeRepository, times(1)).existsByUserIdAndTweetId(testUserId, testTweetId);
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when liking tweet by non-existent user")
    void shouldThrowExceptionWhenLikingTweetByNonExistentUser() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            likeService.likeTweet(testTweetId, UUID.randomUUID());
        });

        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(tweetRepository, never()).findById(any(UUID.class));
        verify(likeRepository, never()).existsByUserIdAndTweetId(any(UUID.class), any(UUID.class));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when liking non-existent tweet")
    void shouldThrowExceptionWhenLikingNonExistentTweet() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            likeService.likeTweet(UUID.randomUUID(), testUserId);
        });

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(any(UUID.class));
        verify(likeRepository, never()).existsByUserIdAndTweetId(any(UUID.class), any(UUID.class));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("should throw CustomValidationException when user already liked the tweet")
    void shouldThrowExceptionWhenUserAlreadyLikedTweet() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));
        when(likeRepository.existsByUserIdAndTweetId(testUserId, testTweetId)).thenReturn(true); // Zaten beğenilmiş

        CustomValidationException exception = assertThrows(CustomValidationException.class, () -> {
            likeService.likeTweet(testTweetId, testUserId);
        });

        assertTrue(exception.getMessage().contains("already liked tweet"));

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(likeRepository, times(1)).existsByUserIdAndTweetId(testUserId, testTweetId);
        verify(likeRepository, never()).save(any(Like.class));
    }

    // --- dislikeTweet Metodu Testleri ---

    @Test
    @DisplayName("should dislike a tweet successfully")
    void shouldDislikeTweetSuccessfully() {
        when(likeRepository.findByUserIdAndTweetId(testUserId, testTweetId)).thenReturn(Optional.of(testLike));
        doNothing().when(likeRepository).delete(any(Like.class)); // delete metodunun bir şey yapmamasını mock'la

        likeService.dislikeTweet(testTweetId, testUserId);

        verify(likeRepository, times(1)).findByUserIdAndTweetId(testUserId, testTweetId);
        verify(likeRepository, times(1)).delete(testLike);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when disliking non-existent like")
    void shouldThrowExceptionWhenDislikingNonExistentLike() {
        when(likeRepository.findByUserIdAndTweetId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            likeService.dislikeTweet(UUID.randomUUID(), UUID.randomUUID());
        });

        verify(likeRepository, times(1)).findByUserIdAndTweetId(any(UUID.class), any(UUID.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    // --- getLikesForTweet Metodu Testleri ---

    @Test
    @DisplayName("should return list of likes for a given tweet")
    void shouldReturnListOfLikesForGivenTweet() {
        List<Like> likes = List.of(testLike, Like.builder().id(UUID.randomUUID()).user(User.builder().id(UUID.randomUUID()).build()).tweet(testTweet).createdAt(LocalDateTime.now()).build());
        when(tweetRepository.existsById(testTweetId)).thenReturn(true);
        when(likeRepository.findByTweetId(testTweetId)).thenReturn(likes);

        List<Like> result = likeService.getLikesForTweet(testTweetId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testLike.getId(), result.get(0).getId());

        verify(tweetRepository, times(1)).existsById(testTweetId);
        verify(likeRepository, times(1)).findByTweetId(testTweetId);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when getting likes for non-existent tweet")
    void shouldThrowExceptionWhenGettingLikesForNonExistentTweet() {
        when(tweetRepository.existsById(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            likeService.getLikesForTweet(UUID.randomUUID());
        });

        verify(tweetRepository, times(1)).existsById(any(UUID.class));
        verify(likeRepository, never()).findByTweetId(any(UUID.class));
    }

    // --- hasUserLikedTweet Metodu Testleri ---

    @Test
    @DisplayName("should return true if user liked the tweet")
    void shouldReturnTrueIfUserLikedTweet() {
        when(likeRepository.existsByUserIdAndTweetId(testUserId, testTweetId)).thenReturn(true);

        boolean result = likeService.hasUserLikedTweet(testTweetId, testUserId);

        assertTrue(result);
        verify(likeRepository, times(1)).existsByUserIdAndTweetId(testUserId, testTweetId);
    }

    @Test
    @DisplayName("should return false if user did not like the tweet")
    void shouldReturnFalseIfUserDidNotLikeTweet() {
        when(likeRepository.existsByUserIdAndTweetId(testUserId, testTweetId)).thenReturn(false);

        boolean result = likeService.hasUserLikedTweet(testTweetId, testUserId);

        assertFalse(result);
        verify(likeRepository, times(1)).existsByUserIdAndTweetId(testUserId, testTweetId);
    }
}
