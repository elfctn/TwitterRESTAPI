package com.fsweb.twitterapi.dto.tweet;

import jakarta.validation.constraints.NotBlank; // Boş olmama validasyonu için
import jakarta.validation.constraints.Size; // Uzunluk validasyonu için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için

import java.util.UUID; // Eğer bir yanıt veya retweet ise, ilgili Tweet'in ID'si için

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TweetCreateRequest {

    @NotBlank(message = "Tweet content cannot be empty") // Tweet içeriği boş olamaz
    @Size(max = 280, message = "Tweet content cannot exceed 280 characters") // Maksimum 280 karakter
    private String content;

    // Eğer bu bir yanıt tweeti ise, yanıt verilen tweet'in ID'si. Opsiyonel.
    private UUID replyToTweetId;

    // Eğer bu bir retweet ise, orijinal tweet'in ID'si. Opsiyonel.
    // Projemizde retweet'leri ayrı bir entity olarak da tuttuğumuz için burası sadece referans amaçlı.
    // Ancak API tarafında bu bilginin gelmesi önemlidir.
    private UUID originalTweetId;

    // Eğer bu bir retweet ise true, normal tweet ise false.
    // Genellikle frontend tarafından bu bilgi gelir.
    private Boolean isRetweet = false;
}