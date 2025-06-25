package com.fsweb.twitterapi.controller;

import com.fsweb.twitterapi.dto.comment.CommentCreateRequest; // Yorum oluşturma isteği DTO'su
import com.fsweb.twitterapi.dto.comment.CommentUpdateRequest; // Yorum güncelleme isteği DTO'su
import com.fsweb.twitterapi.dto.comment.CommentResponse; // Yorum yanıt DTO'su
import com.fsweb.twitterapi.service.CommentService; // CommentService'i enjekte edeceğiz

import jakarta.validation.Valid; // Validasyon için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.security.access.prepost.PreAuthorize; // Metot bazlı yetkilendirme için
import org.springframework.security.core.Authentication; // Mevcut kimlik doğrulama objesi için
import org.springframework.security.core.context.SecurityContextHolder; // Güvenlik bağlamından kullanıcı bilgilerini almak için
import org.springframework.web.bind.annotation.DeleteMapping; // DELETE isteklerini karşılamak için
import org.springframework.web.bind.annotation.GetMapping; // GET isteklerini karşılamak için
import org.springframework.web.bind.annotation.PathVariable; // URL yol değişkenlerini almak için
import org.springframework.web.bind.annotation.PostMapping; // POST isteklerini karşılamak için
import org.springframework.web.bind.annotation.PutMapping; // PUT isteklerini karşılamak için
import org.springframework.web.bind.annotation.RequestBody; // HTTP isteğinin body'sini Java objesine dönüştürmek için
import org.springframework.web.bind.annotation.RequestMapping; // İstekleri belirli bir URL yoluna eşlemek için
import org.springframework.web.bind.annotation.RestController; // RESTful Controller olduğunu belirtmek için

import java.util.List; // List tipini kullanmak için
import java.util.UUID; // UUID tipi için

@RestController // Bu sınıfın bir REST Controller olduğunu belirtir.
@RequestMapping("/comments") // Bu Controller'daki tüm endpoint'lerin "/comments" yoluyla başlayacağını belirtir.
public class CommentController {

    private final CommentService commentService; // CommentService'i enjekte ediyoruz

    // Constructor Injection
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Belirli bir tweete yeni bir yorum ekler.
     * Endpoint: POST /comments/{tweetId}
     * Erişim: Kimliği doğrulanmış kullanıcılar.
     *
     * @param tweetId Yorumun yapılacağı tweet'in ID'si (URL'den gelir)
     * @param request Yorum içeriği içeren CommentCreateRequest DTO
     * @return Oluşturulan yorumun CommentResponse DTO'su ve HTTP 201 Created durumu
     */
    @PostMapping("/{tweetId}") // POST isteği için "/comments/{tweetId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Bu metoda erişim için kullanıcının kimlik doğrulamasının yapılmış olması gerekir.
    public ResponseEntity<CommentResponse> addComment(@PathVariable UUID tweetId,
                                                      @Valid @RequestBody CommentCreateRequest request) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName()); // authenticated.getName() genellikle username/email verir

        CommentResponse newComment = commentService.addComment(request, tweetId, currentUserId); // CommentService'i çağırarak yorum ekle
        return new ResponseEntity<>(newComment, HttpStatus.CREATED); // HTTP 201 Created durum kodu ile yanıt dön
    }

    /**
     * Belirli bir tweete ait tüm yorumları getirir.
     * Endpoint: GET /comments/tweet/{tweetId}
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param tweetId Yorumları getirilecek tweet'in ID'si
     * @return Yorumların List<CommentResponse> DTO'su ve HTTP 200 OK durumu
     */
    @GetMapping("/tweet/{tweetId}") // GET isteği için "/comments/tweet/{tweetId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<List<CommentResponse>> getCommentsByTweetId(@PathVariable UUID tweetId) {
        List<CommentResponse> comments = commentService.getCommentsByTweetId(tweetId); // CommentService'i çağırarak yorumları getir
        return ResponseEntity.ok(comments); // HTTP 200 OK durum kodu ile yanıt dön
    }

    /**
     * Belirli bir yorumu ID'sine göre günceller.
     * Endpoint: PUT /comments/{id}
     * Erişim: Sadece yorum sahibi güncelleyebilir.
     *
     * @param id Güncellenecek yorumun UUID ID'si
     * @param request Yorum güncelleme bilgileri içeren CommentUpdateRequest DTO
     * @return Güncellenen yorumun CommentResponse DTO'su ve HTTP 200 OK durumu
     */
    @PutMapping("/{id}") // PUT isteği için "/comments/{id}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<CommentResponse> updateComment(@PathVariable UUID id,
                                                         @Valid @RequestBody CommentUpdateRequest request) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName());

        CommentResponse updatedComment = commentService.updateComment(id, request, currentUserId); // CommentService'i çağırarak yorumu güncelle
        return ResponseEntity.ok(updatedComment); // HTTP 200 OK durum kodu ile yanıt dön
    }

    /**
     * Belirli bir yorumu ID'sine göre siler.
     * Endpoint: DELETE /comments/{id}
     * Erişim: Yorum sahibi veya yorumun yapıldığı tweet'in sahibi silebilir.
     *
     * @param id Silinecek yorumun UUID ID'si
     * @return HTTP 204 No Content durumu (başarılı silme)
     */
    @DeleteMapping("/{id}") // DELETE isteği için "/comments/{id}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName());

        commentService.deleteComment(id, currentUserId); // CommentService'i çağırarak yorumu sil
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // HTTP 204 No Content durum kodu ile yanıt dön
    }
}
