package com.fsweb.twitterapi.dto.comment;

import jakarta.validation.constraints.NotBlank; // Boş olmama validasyonu için
import jakarta.validation.constraints.Size; // Uzunluk validasyonu için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateRequest {

    @NotBlank(message = "Comment content cannot be empty") // Yorum içeriği boş olamaz
    @Size(max = 200, message = "Comment content cannot exceed 200 characters") // Maksimum 200 karakter
    private String content;
    // Comment oluşturulurken hangi tweete ait olduğu Controller veya Service katmanında URL'den veya request body'den alınabilir.
    // Hangi kullanıcıya ait olduğu ise genellikle güvenlik bağlamından (token'dan) alınır.
}