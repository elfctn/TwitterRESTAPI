package com.fsweb.twitterapi; // Paket adı güncellendi

import com.fsweb.twitterapi.entity.Retweet; // Retweet entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz
import com.fsweb.twitterapi.exception.CustomValidationException; // Özel validasyon istisnamızı import ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadı istisnamızı import ediyoruz
import com.fsweb.twitterapi.repository.RetweetRepository; // RetweetRepository'yi mock'layacağız
import com.fsweb.twitterapi.repository.TweetRepository; // TweetRepository'yi mock'layacağız
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi mock'layacağız
import com.fsweb.twitterapi.service.RetweetService; // Test edeceğimiz Service sınıfı
import com.fsweb.twitterapi.dto.retweet.RetweetRequest; // Retweet DTO'sumuzu import ediyoruz

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
@DisplayName("RetweetService Unit Tests") // Test sınıfına okunabilir bir isim verir
public class RetweetServiceTest {

    @Mock // RetweetRepository'nin sahte bir versiyonunu oluşturur.
    private RetweetRepository retweetRepository;

    @Mock // TweetRepository'nin sahte bir versiyonunu oluşturur.
    private TweetRepository tweetRepository;

    @Mock // UserRepository'nin sahte bir versiyonunu oluşturur.
    private UserRepository userRepository;

    @InjectMocks // Test edilecek gerçek RetweetService nesnesini oluşturur ve bağımlılıkları enjekte eder.
    private RetweetService retweetService;

    // Testlerde kullanılacak örnek veri
    private User testUser;
    private Tweet originalTweet;
    private Retweet testRetweet;
    private UUID testUserId;
    private UUID originalTweetId;
    private UUID testRetweetId;

    @BeforeEach // Her test metodundan önce çalışacak kurulum metodu.
    void setUp() {
        testUserId = UUID.randomUUID();
        originalTweetId = UUID.randomUUID();
        testRetweetId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .username("retweeteruser")
                .email("retweeter@example.com")
                .password("encodedpass")
                .createdAt(LocalDateTime.now())
                .build();

        originalTweet = Tweet.builder()
                .id(originalTweetId)
                .content("Original Tweet Content for Retweet")
                .user(User.builder().id(UUID.randomUUID()).username("originaltweetowner").build())
                .createdAt(LocalDateTime.now())
                .build();

        testRetweet = Retweet.builder()
                .id(testRetweetId)
                .user(testUser)
                .originalTweet(originalTweet)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- retweetTweet Metodu Testleri ---

    @Test
    @DisplayName("should retweet a tweet successfully")
    void shouldRetweetTweetSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(originalTweetId)).thenReturn(Optional.of(originalTweet));
        when(retweetRepository.existsByUserIdAndOriginalTweetId(testUserId, originalTweetId)).thenReturn(false); // Henüz retweet edilmemiş
        when(retweetRepository.save(any(Retweet.class))).thenReturn(testRetweet);

        Retweet result = retweetService.retweetTweet(originalTweetId, testUserId);

        assertNotNull(result);
        assertEquals(testRetweetId, result.getId());
        assertEquals(testUserId, result.getUser().getId());
        assertEquals(originalTweetId, result.getOriginalTweet().getId());

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(originalTweetId);
        verify(retweetRepository, times(1)).existsByUserIdAndOriginalTweetId(testUserId, originalTweetId);
        verify(retweetRepository, times(1)).save(any(Retweet.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when retweeting by non-existent user")
    void shouldThrowExceptionWhenRetweetingByNonExistentUser() {
        // Düzeltme: Orijinal tweet'i bulma çağrısının başarılı olmasını sağla, böylece kullanıcı aramasına geçilebilir.
        when(tweetRepository.findById(originalTweetId)).thenReturn(Optional.of(originalTweet));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Kullanıcı bulunamıyor

        assertThrows(ResourceNotFoundException.class, () -> {
            retweetService.retweetTweet(originalTweetId, UUID.randomUUID());
        });

        // Düzeltme: tweetRepository.findById çağrısının yapıldığını doğrula
        verify(tweetRepository, times(1)).findById(originalTweetId);
        // Düzeltme: userRepository.findById çağrısının yapıldığını doğrula
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(retweetRepository, never()).existsByUserIdAndOriginalTweetId(any(UUID.class), any(UUID.class));
        verify(retweetRepository, never()).save(any(Retweet.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when retweeting non-existent original tweet")
    void shouldThrowExceptionWhenRetweetingNonExistentOriginalTweet() {
        // Düzeltme: Kullanıcıyı bulma çağrısının hiç yapılmamasını bekle.
        // retweetTweet metodunda önce Original Tweet bulunur, o bulunamazsa kod userRepository.findById'a ulaşmaz.
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Orijinal tweet bulunamıyor

        assertThrows(ResourceNotFoundException.class, () -> {
            retweetService.retweetTweet(UUID.randomUUID(), testUserId); // Rastgele bir tweet ID'si ile arama yap
        });

        // Düzeltme: userRepository.findById çağrısı hiç yapılmamalıdır.
        verify(userRepository, never()).findById(any(UUID.class)); // Bu satır düzeltildi.
        verify(tweetRepository, times(1)).findById(any(UUID.class));
        verify(retweetRepository, never()).existsByUserIdAndOriginalTweetId(any(UUID.class), any(UUID.class));
        verify(retweetRepository, never()).save(any(Retweet.class));
    }

    @Test
    @DisplayName("should throw CustomValidationException when user already retweeted the tweet")
    void shouldThrowExceptionWhenUserAlreadyRetweetedTweet() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(originalTweetId)).thenReturn(Optional.of(originalTweet));
        when(retweetRepository.existsByUserIdAndOriginalTweetId(testUserId, originalTweetId)).thenReturn(true); // Zaten retweet edilmiş

        CustomValidationException exception = assertThrows(CustomValidationException.class, () -> {
            retweetService.retweetTweet(originalTweetId, testUserId);
        });

        assertTrue(exception.getMessage().contains("already retweeted tweet"));

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).findById(originalTweetId);
        verify(retweetRepository, times(1)).existsByUserIdAndOriginalTweetId(testUserId, originalTweetId);
        verify(retweetRepository, never()).save(any(Retweet.class));
    }

    // --- unretweetTweet Metodu Testleri ---

    @Test
    @DisplayName("should unretweet a tweet successfully")
    void shouldUnretweetTweetSuccessfully() {
        when(retweetRepository.findByUserIdAndOriginalTweetId(testUserId, originalTweetId)).thenReturn(Optional.of(testRetweet));
        doNothing().when(retweetRepository).delete(any(Retweet.class)); // delete metodunun bir şey yapmamasını mock'la

        retweetService.unretweetTweet(originalTweetId, testUserId);

        verify(retweetRepository, times(1)).findByUserIdAndOriginalTweetId(testUserId, originalTweetId);
        verify(retweetRepository, times(1)).delete(testRetweet);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when unretweeting non-existent retweet")
    void shouldThrowExceptionWhenUnretweetingNonExistentRetweet() {
        when(retweetRepository.findByUserIdAndOriginalTweetId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            retweetService.unretweetTweet(UUID.randomUUID(), UUID.randomUUID());
        });

        verify(retweetRepository, times(1)).findByUserIdAndOriginalTweetId(any(UUID.class), any(UUID.class));
        verify(retweetRepository, never()).delete(any(Retweet.class));
    }

    // --- getRetweetsForOriginalTweet Metodu Testleri ---

    @Test
    @DisplayName("should return list of retweets for a given original tweet")
    void shouldReturnListOfRetweetsForGivenOriginalTweet() {
        List<Retweet> retweets = List.of(testRetweet, Retweet.builder().id(UUID.randomUUID()).user(User.builder().id(UUID.randomUUID()).build()).originalTweet(originalTweet).createdAt(LocalDateTime.now()).build());
        when(tweetRepository.existsById(originalTweetId)).thenReturn(true);
        when(retweetRepository.findByOriginalTweetId(originalTweetId)).thenReturn(retweets);

        List<Retweet> result = retweetService.getRetweetsForOriginalTweet(originalTweetId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testRetweet.getId(), result.get(0).getId());

        verify(tweetRepository, times(1)).existsById(originalTweetId);
        verify(retweetRepository, times(1)).findByOriginalTweetId(originalTweetId);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when getting retweets for non-existent original tweet")
    void shouldThrowExceptionWhenGettingRetweetsForNonExistentOriginalTweet() {
        when(tweetRepository.existsById(any(UUID.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            retweetService.getRetweetsForOriginalTweet(UUID.randomUUID());
        });

        verify(tweetRepository, times(1)).existsById(any(UUID.class));
        verify(retweetRepository, never()).findByOriginalTweetId(any(UUID.class));
    }

    // --- hasUserRetweetedTweet Metodu Testleri ---

    @Test
    @DisplayName("should return true if user retweeted the tweet")
    void shouldReturnTrueIfUserRetweetedTweet() {
        when(retweetRepository.existsByUserIdAndOriginalTweetId(testUserId, originalTweetId)).thenReturn(true);

        boolean result = retweetService.hasUserRetweetedTweet(originalTweetId, testUserId);

        assertTrue(result);
        verify(retweetRepository, times(1)).existsByUserIdAndOriginalTweetId(testUserId, originalTweetId);
    }

    @Test
    @DisplayName("should return false if user did not retweet the tweet")
    void shouldReturnFalseIfUserDidNotRetweetTweet() {
        when(retweetRepository.existsByUserIdAndOriginalTweetId(testUserId, originalTweetId)).thenReturn(false);

        boolean result = retweetService.hasUserRetweetedTweet(originalTweetId, testUserId);

        assertFalse(result);
        verify(retweetRepository, times(1)).existsByUserIdAndOriginalTweetId(testUserId, originalTweetId);
    }
}
