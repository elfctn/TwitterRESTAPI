package com.fsweb.twitterapi.entity;

import jakarta.persistence.*; // JPA anotasyonları için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için
import org.hibernate.annotations.CreationTimestamp; // Hibernate: Oluşturulma zamanı için
import org.hibernate.annotations.GenericGenerator; // Hibernate: UUID üretimi için

import java.time.LocalDateTime; // Tarih ve saat tipi için
import java.util.UUID; // Benzersiz ID (UUID) tipi için

@Entity // Bu sınıfın bir JPA Entity'si olduğunu belirtir.
@Table(name = "likes", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "tweet_id"})})
// Veritabanındaki tablo adını "likes" olarak belirler.
// user_id ve tweet_id sütunlarının birleşimi benzersiz olmalıdır.
// Bu, bir kullanıcının aynı tweete birden fazla kez beğeni atmasını engeller.
@Data // Getter, Setter vb. sağlar.
@NoArgsConstructor // Argümansız constructor.
@AllArgsConstructor // Tüm argümanları alan constructor.
@Builder // Builder deseni.
public class Like {

    @Id // Birincil anahtar
    @GeneratedValue(generator = "UUID") // ID'nin UUID olarak otomatik üretileceğini belirtir.
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Beğeni için benzersiz ana ID.

    @ManyToOne(fetch = FetchType.LAZY) // Bir kullanıcı bir sürü beğeni yapabilir.
    @JoinColumn(name = "user_id", nullable = false) // `likes` tablosundaki `user_id` sütununun, `users` tablosundaki ID'ye referans verdiğini belirtir. Boş olamaz.
    private User user; // Beğeniyi yapan kullanıcı nesnesi.

    @ManyToOne(fetch = FetchType.LAZY) // Bir tweete birden fazla beğeni gelebilir.
    @JoinColumn(name = "tweet_id", nullable = false) // `likes` tablosundaki `tweet_id` sütununun, `tweets` tablosundaki ID'ye referans verdiğini belirtir. Boş olamaz.
    private Tweet tweet; // Beğenilen tweet nesnesi.

    @CreationTimestamp // Beğeninin oluşturulma zamanı. Sadece oluşturulma zamanı yeterli çünkü beğeni içeriği güncellenmez.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}