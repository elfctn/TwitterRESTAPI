package com.fsweb.twitterapi.entity;

import jakarta.persistence.*; // JPA anotasyonları için
import jakarta.validation.constraints.NotBlank; // Boş olmama validasyonu için
import jakarta.validation.constraints.Size; // Uzunluk validasyonu için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için
import org.hibernate.annotations.CreationTimestamp; // Hibernate: Oluşturulma zamanı için
import org.hibernate.annotations.GenericGenerator; // Hibernate: UUID üretimi için
import org.hibernate.annotations.UpdateTimestamp; // Hibernate: Güncellenme zamanı için

import java.time.LocalDateTime; // Tarih ve saat tipi için
import java.util.UUID; // Benzersiz ID (UUID) tipi için

@Entity // Bu sınıfın bir JPA Entity'si olduğunu, yani bir veritabanı tablosuna karşılık geldiğini belirtir.
@Table(name = "comments") // Veritabanındaki tablo adını "comments" olarak belirler.
@Data // Lombok anotasyonu: Getter, Setter, equals(), hashCode() ve toString() metodlarını otomatik olarak oluşturur.
@NoArgsConstructor // Lombok anotasyonu: Argümansız bir constructor oluşturur.
@AllArgsConstructor // Lombok anotasyonu: Sınıftaki tüm alanları içeren bir constructor oluşturur.
@Builder // Lombok anotasyonu: Sınıf için Builder deseni sağlar.
public class Comment {

    @Id // Birincil anahtar
    @GeneratedValue(generator = "UUID") // UUID generator kullan
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Yorum ID'si

    @ManyToOne(fetch = FetchType.LAZY) // Bir yorum, sadece bir tweete aittir. Many (Comment) to One (Tweet) ilişkisi.
    @JoinColumn(name = "tweet_id", nullable = false) // `comments` tablosundaki `tweet_id` sütununun, `tweets` tablosundaki ID'ye referans verdiğini belirtir. Boş olamaz.
    private Tweet tweet; // Yorumun yapıldığı tweet nesnesi.

    @ManyToOne(fetch = FetchType.LAZY) // Bir yorum, sadece bir kullanıcı tarafından yapılır. Many (Comment) to One (User) ilişkisi.
    @JoinColumn(name = "user_id", nullable = false) // `comments` tablosundaki `user_id` sütununun, `users` tablosundaki ID'ye referans verdiğini belirtir. Boş olamaz.
    private User user; // Yorumu yapan kullanıcı nesnesi.

    @Column(name = "content", nullable = false, length = 200) // Yorum içeriği, boş olamaz ve maksimum 200 karakter.
    @NotBlank(message = "Comment content cannot be empty") // Validasyon: İçerik boş olamaz.
    @Size(max = 200, message = "Comment content cannot exceed 200 characters") // Validasyon: Maksimum karakter sayısı.
    private String content;

    @CreationTimestamp // Yorumun oluşturulma zamanı
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Yorumun son güncellenme zamanı (yorum içeriği güncellenebilir)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}