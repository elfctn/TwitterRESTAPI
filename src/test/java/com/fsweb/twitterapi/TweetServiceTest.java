package com.fsweb.twitterapi;

import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadı istisnamızı import ediyoruz
import com.fsweb.twitterapi.exception.UnauthorizedException; // Yetkisiz işlem istisnamızı import ediyoruz
import com.fsweb.twitterapi.repository.TweetRepository; // TweetRepository'yi mock'layacağız
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi mock'layacağız
import com.fsweb.twitterapi.service.TweetService; // Test edeceğimiz Service sınıfı
import com.fsweb.twitterapi.dto.tweet.TweetCreateRequest; // DTO'larımızı import ediyoruz
import com.fsweb.twitterapi.dto.tweet.TweetUpdateRequest;
import com.fsweb.twitterapi.dto.tweet.TweetResponse;

import org.junit.jupiter.api.BeforeEach; // Her test metodundan önce çalışacak kurulum için
import org.junit.jupiter.api.DisplayName; // Test metodlarına daha okunabilir isimler vermek için
import org.junit.jupiter.api.Test; // Bir test metodu olduğunu belirtmek için
import org.junit.jupiter.api.extension.ExtendWith; // Mockito'yu JUnit 5 ile entegre etmek için
import org.mockito.InjectMocks; // Mock'lanmış bağımlılıkları enjekte etmek için
import org.mockito.Mock; // Sahte (mock) bağımlılıklar oluşturmak için
import org.mockito.junit.jupiter.MockitoExtension; // Mockito uzantısı için

import java.time.LocalDateTime; // Tarih ve saat tipi için
import java.util.Arrays; // Listeleri kolayca oluşturmak için
import java.util.List; // List tipi için
import java.util.Optional; // Optional kullanmak için
import java.util.UUID; // UUID tipi için

import static org.junit.jupiter.api.Assertions.*; // JUnit assert metotları için
import static org.mockito.ArgumentMatchers.any; // Herhangi bir argümanı eşleştirmek için
import static org.mockito.Mockito.*; // Mockito metotları için

@ExtendWith(MockitoExtension.class) // JUnit 5'i Mockito ile entegre eder.
@DisplayName("TweetService Unit Tests") // Test sınıfına okunabilir bir isim verir
public class TweetServiceTest {

    @Mock // TweetRepository'nin sahte bir versiyonunu oluşturur.
    private TweetRepository tweetRepository;

    @Mock // UserRepository'nin sahte bir versiyonunu oluşturur.
    private UserRepository userRepository;

    @InjectMocks // Test edilecek gerçek TweetService nesnesini oluşturur ve bağımlılıkları enjekte eder.
    private TweetService tweetService;

    // Testlerde kullanılacak örnek veri
    private User testUser;
    private Tweet originalTweet;
    private Tweet replyTweet;
    private Tweet testTweet;
    private TweetCreateRequest createRequest;
    private TweetCreateRequest createReplyRequest;
    private TweetCreateRequest createRetweetRequest;
    private TweetUpdateRequest updateRequest;
    private UUID testUserId;
    private UUID testTweetId;
    private UUID originalTweetId;
    private UUID replyTweetId;

    @BeforeEach // Her test metodundan önce çalışacak kurulum metodu.
    void setUp() {
        testUserId = UUID.randomUUID();
        testTweetId = UUID.randomUUID();
        originalTweetId = UUID.randomUUID();
        replyTweetId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedpassword")
                .createdAt(LocalDateTime.now())
                .build();

        originalTweet = Tweet.builder()
                .id(originalTweetId)
                .content("Original Tweet Content")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .isRetweet(false)
                .build();

        replyTweet = Tweet.builder()
                .id(replyTweetId)
                .content("Reply Content")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .isRetweet(false)
                .replyToTweet(originalTweet) // Yanıt verilen tweet
                .build();

        testTweet = Tweet.builder()
                .id(testTweetId)
                .content("Test Tweet Content")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .isRetweet(false)
                .build();

        createRequest = TweetCreateRequest.builder()
                .content("New tweet content")
                .isRetweet(false)
                .build();

        createReplyRequest = TweetCreateRequest.builder()
                .content("This is a reply")
                .replyToTweetId(originalTweetId)
                .isRetweet(false)
                .build();

        createRetweetRequest = TweetCreateRequest.builder()
                .content("") // Retweetin kendi içeriği genelde boş olur veya orijinal tweeti tekrarlar
                .originalTweetId(originalTweetId)
                .isRetweet(true)
                .build();

        updateRequest = TweetUpdateRequest.builder()
                .content("Updated tweet content")
                .build();
    }

    // --- createTweet Metodu Testleri ---

    @Test
    @DisplayName("should create a basic tweet successfully")
    void shouldCreateBasicTweetSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.save(any(Tweet.class))).thenReturn(testTweet); // Kaydedilecek tweet testTweet olsun

        TweetResponse result = tweetService.createTweet(createRequest, testUserId);

        assertNotNull(result);
        assertEquals(testTweetId, result.getId());
        assertEquals(createRequest.getContent(), result.getContent());
        assertFalse(result.getIsRetweet());
        assertEquals(testUser.getUsername(), result.getUser().getUsername()); // UserResponse dönüşümü kontrolü

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    @DisplayName("should create a reply tweet successfully")
    void shouldCreateReplyTweetSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(originalTweetId)).thenReturn(Optional.of(originalTweet));
        // Save metodu için, replyTweet entity'sini döndürecek mock davranışı ayarlıyoruz
        when(tweetRepository.save(any(Tweet.class))).thenReturn(replyTweet);

        TweetResponse result = tweetService.createTweet(createReplyRequest, testUserId);

        assertNotNull(result);
        assertEquals(replyTweetId, result.getId());
        assertEquals(createReplyRequest.getContent(), result.getContent());
        assertEquals(originalTweetId, result.getReplyToTweetId()); // Yanıt ID'sinin doğru olduğunu kontrol et
        assertFalse(result.getIsRetweet());

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(originalTweetId);
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    @DisplayName("should create a retweet successfully")
    void shouldCreateRetweetSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(originalTweetId)).thenReturn(Optional.of(originalTweet));
        // Save metodu için, isRetweet true olan bir tweet döndürecek şekilde mock ayarlıyoruz
        Tweet savedRetweet = Tweet.builder()
                .id(UUID.randomUUID())
                .content(createRetweetRequest.getContent())
                .user(testUser)
                .originalTweet(originalTweet)
                .isRetweet(true)
                .createdAt(LocalDateTime.now())
                .build();
        when(tweetRepository.save(any(Tweet.class))).thenReturn(savedRetweet);

        TweetResponse result = tweetService.createTweet(createRetweetRequest, testUserId);

        assertNotNull(result);
        assertEquals(originalTweetId, result.getOriginalTweetId()); // Orijinal tweet ID'sinin doğru olduğunu kontrol et
        assertTrue(result.getIsRetweet()); // isRetweet'in true olduğunu kontrol et

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(originalTweetId);
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when creating tweet with non-existent user")
    void shouldThrowExceptionWhenCreatingTweetWithNonExistentUser() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            tweetService.createTweet(createRequest, UUID.randomUUID());
        });

        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when replying to non-existent tweet")
    void shouldThrowExceptionWhenReplyingToNonExistentTweet() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Reply tweet bulunamıyor

        assertThrows(ResourceNotFoundException.class, () -> {
            tweetService.createTweet(createReplyRequest, testUserId);
        });

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(createReplyRequest.getReplyToTweetId());
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when retweeting non-existent original tweet")
    void shouldThrowExceptionWhenRetweetingNonExistentOriginalTweet() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Original tweet bulunamıyor

        assertThrows(ResourceNotFoundException.class, () -> {
            tweetService.createTweet(createRetweetRequest, testUserId);
        });

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(createRetweetRequest.getOriginalTweetId());
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    // --- getTweetById Metodu Testleri ---

    @Test
    @DisplayName("should return tweet by id successfully")
    void shouldReturnTweetByIdSuccessfully() {
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));

        TweetResponse result = tweetService.getTweetById(testTweetId);

        assertNotNull(result);
        assertEquals(testTweetId, result.getId());
        assertEquals(testTweet.getContent(), result.getContent());
        assertEquals(testUser.getUsername(), result.getUser().getUsername());

        verify(tweetRepository, times(1)).findById(testTweetId);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when tweet not found by id")
    void shouldThrowExceptionWhenTweetNotFoundById() {
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            tweetService.getTweetById(UUID.randomUUID());
        });

        verify(tweetRepository, times(1)).findById(any(UUID.class));
    }

    // --- getTweetsByUserId Metodu Testleri ---

    @Test
    @DisplayName("should return list of tweets for a given user id")
    void shouldReturnListOfTweetsForGivenUserId() {
        List<Tweet> tweets = Arrays.asList(testTweet, originalTweet); // Test için iki örnek tweet
        when(tweetRepository.findByUserId(testUserId)).thenReturn(tweets);

        List<TweetResponse> result = tweetService.getTweetsByUserId(testUserId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testTweet.getId(), result.get(0).getId());
        assertEquals(originalTweet.getId(), result.get(1).getId());
        // Dönüşümün doğru çalıştığını basitçe doğrula
        assertEquals(testUser.getUsername(), result.get(0).getUser().getUsername());

        verify(tweetRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("should return empty list when no tweets found for user id")
    void shouldReturnEmptyListWhenNoTweetsFoundForUserId() {
        when(tweetRepository.findByUserId(any(UUID.class))).thenReturn(List.of()); // Boş liste döndür

        List<TweetResponse> result = tweetService.getTweetsByUserId(UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(tweetRepository, times(1)).findByUserId(any(UUID.class));
    }

    // --- updateTweet Metodu Testleri ---

    @Test
    @DisplayName("should update tweet successfully when authorized")
    void shouldUpdateTweetSuccessfullyWhenAuthorized() {
        // Mock davranışı: Tweet ve onu güncelleyen kullanıcı aynı
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));
        when(tweetRepository.save(any(Tweet.class))).thenReturn(testTweet); // Güncellenmiş tweet'i döndür

        TweetResponse result = tweetService.updateTweet(testTweetId, updateRequest, testUserId);

        assertNotNull(result);
        assertEquals(updateRequest.getContent(), result.getContent()); // İçeriğin güncellendiğini doğrula
        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when updating non-existent tweet")
    void shouldThrowExceptionWhenUpdatingNonExistentTweet() {
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            tweetService.updateTweet(UUID.randomUUID(), updateRequest, testUserId);
        });

        verify(tweetRepository, times(1)).findById(any(UUID.class));
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("should throw UnauthorizedException when updating tweet by unauthorized user")
    void shouldThrowUnauthorizedExceptionWhenUpdatingTweetByUnauthorizedUser() {
        UUID unauthorizedUserId = UUID.randomUUID();
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));

        assertThrows(UnauthorizedException.class, () -> {
            tweetService.updateTweet(testTweetId, updateRequest, unauthorizedUserId);
        });

        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    // --- deleteTweet Metodu Testleri ---

    @Test
    @DisplayName("should delete tweet successfully when authorized")
    void shouldDeleteTweetSuccessfullyWhenAuthorized() {
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet)); // Tweet'i bul
        doNothing().when(tweetRepository).delete(any(Tweet.class)); // delete metodunun bir şey yapmamasını mock'la

        tweetService.deleteTweet(testTweetId, testUserId); // Metodu çağır

        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(tweetRepository, times(1)).delete(testTweet); // Doğru tweet'in silindiğini doğrula
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when deleting non-existent tweet")
    void shouldThrowExceptionWhenDeletingNonExistentTweet() {
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            tweetService.deleteTweet(UUID.randomUUID(), testUserId);
        });

        verify(tweetRepository, times(1)).findById(any(UUID.class));
        verify(tweetRepository, never()).delete(any(Tweet.class));
    }

    @Test
    @DisplayName("should throw UnauthorizedException when deleting tweet by unauthorized user")
    void shouldThrowUnauthorizedExceptionWhenDeletingTweetByUnauthorizedUser() {
        UUID unauthorizedUserId = UUID.randomUUID();
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));

        assertThrows(UnauthorizedException.class, () -> {
            tweetService.deleteTweet(testTweetId, unauthorizedUserId);
        });

        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(tweetRepository, never()).delete(any(Tweet.class));
    }
}
