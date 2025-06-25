package com.fsweb.twitterapi.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fsweb.twitterapi.dto.user.UserResponse; // Yorumu yapan kullanıcının bilgilerini dönmek için
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID tweetId; // Hangi tweete yapıldığı bilgisi
    private UserResponse user; // Yorumu yapan kullanıcının bilgileri
}