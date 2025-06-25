package com.fsweb.twitterapi.dto.comment;

import jakarta.validation.constraints.NotBlank; // Güncellemede de boş olamaz
import jakarta.validation.constraints.Size; // Güncellemede de uzunluk kısıtlaması
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentUpdateRequest {

    @NotBlank(message = "Comment content cannot be empty for update") // İçerik boş olamaz, güncellendiği için.
    @Size(max = 200, message = "Comment content cannot exceed 200 characters") // Maksimum 200 karakter
    private String content;
    // Güncelleme request'inde sadece content alanı bulunur.
    // Diğer bilgiler (yorumun ID'si, kimin yaptığı, hangi tweete ait olduğu)
    // genellikle URL parametreleri veya güvenlik bağlamından alınır.
}