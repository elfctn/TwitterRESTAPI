package com.fsweb.twitterapi;

import com.fsweb.twitterapi.entity.Comment; // Comment entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadı istisnamızı import ediyoruz
import com.fsweb.twitterapi.exception.UnauthorizedException; // Yetkisiz işlem istisnamızı import ediyoruz
import com.fsweb.twitterapi.repository.CommentRepository; // CommentRepository'yi mock'layacağız
import com.fsweb.twitterapi.repository.TweetRepository; // TweetRepository'yi mock'layacağız
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi mock'layacağız
import com.fsweb.twitterapi.service.CommentService; // Test edeceğimiz Service sınıfı
import com.fsweb.twitterapi.dto.comment.CommentCreateRequest; // DTO'larımızı import ediyoruz
import com.fsweb.twitterapi.dto.comment.CommentUpdateRequest;
import com.fsweb.twitterapi.dto.comment.CommentResponse;

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
@DisplayName("CommentService Unit Tests") // Test sınıfına okunabilir bir isim verir
public class CommentServiceTest {

    @Mock // CommentRepository'nin sahte bir versiyonunu oluşturur.
    private CommentRepository commentRepository;

    @Mock // TweetRepository'nin sahte bir versiyonunu oluşturur.
    private TweetRepository tweetRepository;

    @Mock // UserRepository'nin sahte bir versiyonunu oluşturur.
    private UserRepository userRepository;

    @InjectMocks // Test edilecek gerçek CommentService nesnesini oluşturur ve bağımlılıkları enjekte eder.
    private CommentService commentService;

    // Testlerde kullanılacak örnek veri
    private User commentOwnerUser;
    private User tweetOwnerUser; // Tweet sahibini temsil eden kullanıcı
    private User otherUser;      // Başka bir kullanıcı
    private Tweet testTweet;
    private Comment testComment;
    private CommentCreateRequest createRequest;
    private CommentUpdateRequest updateRequest;
    private UUID commentOwnerUserId;
    private UUID tweetOwnerUserId;
    private UUID otherUserId;
    private UUID testTweetId;
    private UUID testCommentId;

    @BeforeEach // Her test metodundan önce çalışacak kurulum metodu.
    void setUp() {
        commentOwnerUserId = UUID.randomUUID();
        tweetOwnerUserId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        testTweetId = UUID.randomUUID();
        testCommentId = UUID.randomUUID();

        commentOwnerUser = User.builder()
                .id(commentOwnerUserId)
                .username("commentowner")
                .email("commentowner@example.com")
                .password("encodedpass")
                .createdAt(LocalDateTime.now())
                .build();

        tweetOwnerUser = User.builder()
                .id(tweetOwnerUserId)
                .username("tweetowner")
                .email("tweetowner@example.com")
                .password("encodedpass")
                .createdAt(LocalDateTime.now())
                .build();

        otherUser = User.builder()
                .id(otherUserId)
                .username("otheruser")
                .email("other@example.com")
                .password("encodedpass")
                .createdAt(LocalDateTime.now())
                .build();

        testTweet = Tweet.builder()
                .id(testTweetId)
                .content("Original tweet content")
                .user(tweetOwnerUser) // Tweet'in sahibi tweetOwnerUser
                .createdAt(LocalDateTime.now())
                .build();

        testComment = Comment.builder()
                .id(testCommentId)
                .content("This is a test comment")
                .tweet(testTweet)
                .user(commentOwnerUser) // Yorumun sahibi commentOwnerUser
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CommentCreateRequest.builder()
                .content("New comment content")
                .build();

        updateRequest = CommentUpdateRequest.builder()
                .content("Updated comment content")
                .build();
    }

    // --- addComment Metodu Testleri ---

    @Test
    @DisplayName("should add comment successfully")
    void shouldAddCommentSuccessfully() {
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));
        when(userRepository.findById(commentOwnerUserId)).thenReturn(Optional.of(commentOwnerUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentResponse result = commentService.addComment(createRequest, testTweetId, commentOwnerUserId);

        assertNotNull(result);
        assertEquals(testCommentId, result.getId());
        assertEquals(createRequest.getContent(), result.getContent());
        assertEquals(testTweetId, result.getTweetId());
        assertEquals(commentOwnerUserId, result.getUser().getId());

        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(userRepository, times(1)).findById(commentOwnerUserId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when adding comment to non-existent tweet")
    void shouldThrowExceptionWhenAddingCommentToNonExistentTweet() {
        when(tweetRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Tweet bulunamıyor

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.addComment(createRequest, UUID.randomUUID(), commentOwnerUserId);
        });

        verify(tweetRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, never()).findById(any(UUID.class)); // Kullanıcı araması yapılmamalı
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when adding comment by non-existent user")
    void shouldThrowExceptionWhenAddingCommentByNonExistentUser() {
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Kullanıcı bulunamıyor

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.addComment(createRequest, testTweetId, UUID.randomUUID());
        });

        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // --- updateComment Metodu Testleri ---

    @Test
    @DisplayName("should update comment successfully when authorized as comment owner")
    void shouldUpdateCommentSuccessfullyWhenAuthorizedAsCommentOwner() {
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment); // Güncellenmiş yorumu döndür

        CommentResponse result = commentService.updateComment(testCommentId, updateRequest, commentOwnerUserId);

        assertNotNull(result);
        assertEquals(updateRequest.getContent(), result.getContent()); // İçeriğin güncellendiğini doğrula
        verify(commentRepository, times(1)).findById(testCommentId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when updating non-existent comment")
    void shouldThrowExceptionWhenUpdatingNonExistentComment() {
        when(commentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.updateComment(UUID.randomUUID(), updateRequest, commentOwnerUserId);
        });

        verify(commentRepository, times(1)).findById(any(UUID.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("should throw UnauthorizedException when updating comment by unauthorized user")
    void shouldThrowUnauthorizedExceptionWhenUpdatingCommentByUnauthorizedUser() {
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

        assertThrows(UnauthorizedException.class, () -> {
            commentService.updateComment(testCommentId, updateRequest, otherUserId); // Başka bir kullanıcı güncelliyor
        });

        verify(commentRepository, times(1)).findById(testCommentId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // --- deleteComment Metodu Testleri ---

    @Test
    @DisplayName("should delete comment successfully when authorized as comment owner")
    void shouldDeleteCommentSuccessfullyAsCommentOwner() {
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));
        doNothing().when(commentRepository).delete(any(Comment.class));

        commentService.deleteComment(testCommentId, commentOwnerUserId); // Yorum sahibi siliyor

        verify(commentRepository, times(1)).findById(testCommentId);
        verify(commentRepository, times(1)).delete(testComment);
    }

    @Test
    @DisplayName("should delete comment successfully when authorized as tweet owner")
    void shouldDeleteCommentSuccessfullyAsTweetOwner() {
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));
        doNothing().when(commentRepository).delete(any(Comment.class));

        commentService.deleteComment(testCommentId, tweetOwnerUserId); // Tweet sahibi siliyor

        verify(commentRepository, times(1)).findById(testCommentId);
        verify(commentRepository, times(1)).delete(testComment);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when deleting non-existent comment")
    void shouldThrowExceptionWhenDeletingNonExistentComment() {
        when(commentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.deleteComment(UUID.randomUUID(), commentOwnerUserId);
        });

        verify(commentRepository, times(1)).findById(any(UUID.class));
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("should throw UnauthorizedException when deleting comment by unauthorized user")
    void shouldThrowUnauthorizedExceptionWhenDeletingCommentByUnauthorizedUser() {
        when(commentRepository.findById(testCommentId)).thenReturn(Optional.of(testComment));

        assertThrows(UnauthorizedException.class, () -> {
            commentService.deleteComment(testCommentId, otherUserId); // Başka bir kullanıcı siliyor
        });

        verify(commentRepository, times(1)).findById(testCommentId);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    // --- getCommentsByTweetId Metodu Testleri ---

    @Test
    @DisplayName("should return list of comments for a given tweet id")
    void shouldReturnListOfCommentsForGivenTweetId() {
        List<Comment> comments = Arrays.asList(testComment, Comment.builder().id(UUID.randomUUID()).content("Another comment").tweet(testTweet).user(otherUser).createdAt(LocalDateTime.now()).build());
        when(commentRepository.findByTweetId(testTweetId)).thenReturn(comments);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(commentOwnerUser), Optional.of(otherUser)); // Mapleme için kullanıcıları mock'la

        List<CommentResponse> result = commentService.getCommentsByTweetId(testTweetId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testComment.getId(), result.get(0).getId());
        assertEquals("Another comment", result.get(1).getContent());

        verify(commentRepository, times(1)).findByTweetId(testTweetId);
    }

    @Test
    @DisplayName("should return empty list when no comments found for tweet id")
    void shouldReturnEmptyListWhenNoCommentsFoundForTweetId() {
        when(commentRepository.findByTweetId(any(UUID.class))).thenReturn(List.of());

        List<CommentResponse> result = commentService.getCommentsByTweetId(UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(commentRepository, times(1)).findByTweetId(any(UUID.class));
    }
}
