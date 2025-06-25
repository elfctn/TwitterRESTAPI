package com.fsweb.twitterapi.service;

import com.fsweb.twitterapi.entity.Comment; // Comment entity'sini import ediyoruz
import com.fsweb.twitterapi.entity.Tweet; // Tweet entity'sini import ediyoruz (Yorumun ait olduğu tweet'i bulmak için)
import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz (Yorumu yapan kullanıcıyı bulmak için)
import com.fsweb.twitterapi.repository.CommentRepository; // CommentRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.repository.TweetRepository; // TweetRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi enjekte ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadığında fırlatılacak istisna
import com.fsweb.twitterapi.exception.UnauthorizedException; // Yetkisiz işlem için istisna
import com.fsweb.twitterapi.dto.comment.CommentCreateRequest; // Yorum oluşturma isteği DTO'su
import com.fsweb.twitterapi.dto.comment.CommentUpdateRequest; // Yorum güncelleme isteği DTO'su
import com.fsweb.twitterapi.dto.comment.CommentResponse; // Yorum yanıt DTO'su
import com.fsweb.twitterapi.dto.user.UserResponse; // UserResponse DTO'su (CommentResponse içinde kullanılacak)

import org.springframework.stereotype.Service; // Bu sınıfın bir Spring Service bileşeni olduğunu belirtmek için
import org.springframework.transaction.annotation.Transactional; // İşlemleri (transaction) yönetmek için

import java.util.List; // List tipini kullanmak için
import java.util.UUID; // UUID tipi için
import java.util.stream.Collectors; // Akış (Stream) API'si için
import java.util.Optional; // Optional kullanmak için

@Service // Bu anotasyon, Spring'e bu sınıfın bir servis bileşeni olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class CommentService {

    private final CommentRepository commentRepository; // Yorum veri erişimi için
    private final TweetRepository tweetRepository;     // Tweet veri erişimi için
    private final UserRepository userRepository;       // Kullanıcı veri erişimi için

    // Constructor Injection
    public CommentService(CommentRepository commentRepository, TweetRepository tweetRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    /**
     * Belirli bir tweete yeni bir yorum ekler.
     *
     * @param request Yorum içeriği içeren DTO
     * @param tweetId Yorumun yapılacağı tweet'in ID'si
     * @param userId Yorumu yapan kullanıcının ID'si (güvenlik bağlamından gelir)
     * @return Oluşturulan yorumun CommentResponse DTO'su
     */
    @Transactional
    public CommentResponse addComment(CommentCreateRequest request, UUID tweetId, UUID userId) {
        // 1. Yorumun yapılacağı tweet'i bul
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResourceNotFoundException("Tweet", "id", tweetId));

        // 2. Yorumu yapan kullanıcıyı bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 3. Comment entity'si oluştur
        Comment comment = Comment.builder()
                .content(request.getContent())
                .tweet(tweet) // Yorumun ait olduğu tweet'i set et
                .user(user)   // Yorumu yapan kullanıcıyı set et
                .build();

        // 4. Yorumu kaydet
        Comment savedComment = commentRepository.save(comment);

        // 5. Kaydedilen Comment entity'sinden CommentResponse DTO'su oluştur ve döndür
        return mapCommentToCommentResponse(savedComment);
    }

    /**
     * Belirli bir yoruma ait bilgileri günceller.
     * Sadece yorum sahibi güncelleyebilir.
     *
     * @param commentId Güncellenecek yorumun ID'si
     * @param request Yorum güncelleme bilgileri içeren DTO
     * @param currentUserId İşlemi yapan mevcut kullanıcının ID'si
     * @return Güncellenen yorumun CommentResponse DTO'su
     */
    @Transactional
    public CommentResponse updateComment(UUID commentId, CommentUpdateRequest request, UUID currentUserId) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // Yetkilendirme kontrolü: Sadece yorum sahibi güncelleyebilir
        if (!existingComment.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not authorized to update this comment.");
        }

        // Sadece içeriği güncelle
        Optional.ofNullable(request.getContent())
                .filter(s -> !s.isEmpty())
                .ifPresent(existingComment::setContent);

        Comment updatedComment = commentRepository.save(existingComment);
        return mapCommentToCommentResponse(updatedComment);
    }

    /**
     * Belirli bir yorumu siler.
     * Sadece yorum sahibi veya yorumun yapıldığı tweet'in sahibi silebilir.
     *
     * @param commentId Silinecek yorumun ID'si
     * @param currentUserId İşlemi yapan mevcut kullanıcının ID'si
     */
    @Transactional
    public void deleteComment(UUID commentId, UUID currentUserId) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // Yetkilendirme kontrolü: Yorum sahibi VEYA Tweet sahibi silebilir
        boolean isCommentOwner = existingComment.getUser().getId().equals(currentUserId);
        boolean isTweetOwner = existingComment.getTweet().getUser().getId().equals(currentUserId);

        if (!isCommentOwner && !isTweetOwner) {
            throw new UnauthorizedException("You are not authorized to delete this comment.");
        }

        commentRepository.delete(existingComment);
    }

    /**
     * Belirli bir tweete ait tüm yorumları getirir.
     *
     * @param tweetId Yorumları getirilecek tweet'in ID'si
     * @return Yorumların List<CommentResponse> DTO'su
     */
    public List<CommentResponse> getCommentsByTweetId(UUID tweetId) {
        // Yorumları çekerken Lazy loading nedeniyle tweet ve user objeleri henüz dolu olmayabilir.
        // mapCommentToCommentResponse metodunda UserResponse oluşturulurken bu alanlara erişildiğinde doldurulacak.
        List<Comment> comments = commentRepository.findByTweetId(tweetId);
        return comments.stream()
                .map(this::mapCommentToCommentResponse)
                .collect(Collectors.toList());
    }

    // --- Yardımcı Metotlar ---

    /**
     * Comment entity'sinden CommentResponse DTO'suna dönüşüm yapar.
     * İlişkili User bilgisini UserResponse DTO'su olarak dahil eder.
     *
     * @param comment Dönüştürülecek Comment entity'si
     * @return CommentResponse DTO'su
     */
    private CommentResponse mapCommentToCommentResponse(Comment comment) {
        // Yorumu yapan kullanıcının bilgilerini UserResponse DTO'suna dönüştür
        UserResponse userResponse = UserResponse.builder()
                .id(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .email(comment.getUser().getEmail())
                .name(comment.getUser().getName())
                .surname(comment.getUser().getSurname())
                .bio(comment.getUser().getBio())
                .profileImageUrl(comment.getUser().getProfileImageUrl())
                .createdAt(comment.getUser().getCreatedAt())
                .updatedAt(comment.getUser().getUpdatedAt())
                .build();

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .tweetId(comment.getTweet().getId()) // Yorumun ait olduğu tweet'in ID'si
                .user(userResponse) // Yorumu yapan kullanıcı bilgisi
                .build();
    }
}