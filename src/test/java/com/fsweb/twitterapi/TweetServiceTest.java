package com.fsweb.twitterapi;

import com.fsweb.twitterapi.entity.Tweet;
import com.fsweb.twitterapi.entity.User;
import com.fsweb.twitterapi.exception.ResourceNotFoundException;
import com.fsweb.twitterapi.exception.UnauthorizedException;
import com.fsweb.twitterapi.repository.TweetRepository;
import com.fsweb.twitterapi.repository.UserRepository;
import com.fsweb.twitterapi.service.TweetService;
import com.fsweb.twitterapi.dto.tweet.TweetCreateRequest;
import com.fsweb.twitterapi.dto.tweet.TweetUpdateRequest;
import com.fsweb.twitterapi.dto.tweet.TweetResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TweetService Unit Tests")
public class TweetServiceTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
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

    @BeforeEach
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
                .replyToTweet(originalTweet)
                .build();

        testTweet = Tweet.builder() // Bu testTweet objesi genel bir placeholder, create metotlarında yeni objeler döneceğiz
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
        // Düzeltme: Save metodu çağrıldığında, createRequest'in içeriğini içeren yeni bir Tweet objesi döndür.
        Tweet savedBasicTweet = Tweet.builder()
                .id(testTweetId)
                .content(createRequest.getContent()) // Request'ten gelen content
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .isRetweet(false)
                .build();
        when(tweetRepository.save(any(Tweet.class))).thenReturn(savedBasicTweet);

        TweetResponse result = tweetService.createTweet(createRequest, testUserId);

        assertNotNull(result);
        assertEquals(testTweetId, result.getId());
        assertEquals(createRequest.getContent(), result.getContent()); // Assertion doğru
        assertFalse(result.getIsRetweet());
        assertEquals(testUser.getUsername(), result.getUser().getUsername());

        verify(userRepository, times(1)).findById(testUserId);
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    @DisplayName("should create a reply tweet successfully")
    void shouldCreateReplyTweetSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(tweetRepository.findById(originalTweetId)).thenReturn(Optional.of(originalTweet));
        // Düzeltme: Save metodu çağrıldığında, createReplyRequest'in içeriğini içeren yeni bir Tweet objesi döndür.
        Tweet savedReplyTweet = Tweet.builder()
                .id(replyTweetId)
                .content(createReplyRequest.getContent()) // Request'ten gelen content
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .isRetweet(false)
                .replyToTweet(originalTweet)
                .build();
        when(tweetRepository.save(any(Tweet.class))).thenReturn(savedReplyTweet);

        TweetResponse result = tweetService.createTweet(createReplyRequest, testUserId);

        assertNotNull(result);
        assertEquals(replyTweetId, result.getId());
        assertEquals(createReplyRequest.getContent(), result.getContent()); // Assertion doğru
        assertEquals(originalTweetId, result.getReplyToTweetId());
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
                .content(createRetweetRequest.getContent()) // Request'ten gelen content (boş olabilir)
                .user(testUser)
                .originalTweet(originalTweet)
                .isRetweet(true) // Retweet olduğu için true
                .createdAt(LocalDateTime.now())
                .build();
        when(tweetRepository.save(any(Tweet.class))).thenReturn(savedRetweet);

        TweetResponse result = tweetService.createTweet(createRetweetRequest, testUserId);

        assertNotNull(result);
        assertEquals(originalTweetId, result.getOriginalTweetId());
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
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

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
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

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
        List<Tweet> tweets = Arrays.asList(testTweet, originalTweet);
        when(tweetRepository.findByUserId(testUserId)).thenReturn(tweets);

        List<TweetResponse> result = tweetService.getTweetsByUserId(testUserId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testTweet.getId(), result.get(0).getId());
        assertEquals(originalTweet.getId(), result.get(1).getId());
        assertEquals(testUser.getUsername(), result.get(0).getUser().getUsername());

        verify(tweetRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("should return empty list when no tweets found for user id")
    void shouldReturnEmptyListWhenNoTweetsFoundForUserId() {
        when(tweetRepository.findByUserId(any(UUID.class))).thenReturn(List.of());

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
        // Düzeltme: Save metodu çağrıldığında, updateRequest'in içeriğini içeren yeni bir Tweet objesi döndür.
        Tweet updatedTweetEntity = Tweet.builder()
                .id(testTweetId)
                .content(updateRequest.getContent())
                .user(testUser)
                .createdAt(testTweet.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .isRetweet(testTweet.getIsRetweet())
                .replyToTweet(testTweet.getReplyToTweet())
                .originalTweet(testTweet.getOriginalTweet())
                .build();
        when(tweetRepository.save(any(Tweet.class))).thenReturn(updatedTweetEntity);

        TweetResponse result = tweetService.updateTweet(testTweetId, updateRequest, testUserId);

        assertNotNull(result);
        assertEquals(updateRequest.getContent(), result.getContent());
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
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));
        doNothing().when(tweetRepository).delete(any(Tweet.class));

        tweetService.deleteTweet(testTweetId, testUserId);

        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(tweetRepository, times(1)).delete(testTweet);
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
