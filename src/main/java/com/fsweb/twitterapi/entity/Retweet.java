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
@Table(name = "retweets", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "original_tweet_id"})})
// Veritabanındaki tablo adını "retweets" olarak belirler.
// ***YENİ EKLEDİĞİM KISIM: uniqueConstraints***
// user_id ve original_tweet_id sütunlarının birleşimi benzersiz olmalıdır.
// Bu, bir kullanıcının aynı tweeti birden fazla kez retweet etmesini engeller.
@Data // Getter, Setter vb. sağlar.
@NoArgsConstructor // Argümansız constructor.
@AllArgsConstructor // Tüm argümanları alan constructor.
@Builder // Builder deseni.
public class Retweet {

    @Id // Birincil anahtar
    @GeneratedValue(generator = "UUID") // ID'nin UUID olarak otomatik üretileceğini belirtir.
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Retweet için benzersiz ana ID.

    @ManyToOne(fetch = FetchType.LAZY) // Bir kullanıcı birden fazla rt atabilir
    @JoinColumn(name = "user_id", nullable = false) // `retweets` tablosundaki `user_id` sütununun, `users` tablosundaki ID'ye referans verdiğini belirtir. Boş olamaz.
    private User user; // Retweeti yapan kullanıcı nesnesi.

    @ManyToOne(fetch = FetchType.LAZY) // Bir orginal tweet üst üste birden çok rt alabilir.
    @JoinColumn(name = "original_tweet_id", nullable = false) // `retweets` tablosundaki `original_tweet_id` sütununun, `tweets` tablosundaki ID'ye referans verdiğini belirtir. Boş olamaz.
    private Tweet originalTweet; // Retweet edilen orijinal tweet nesnesi.

    @CreationTimestamp // Retweetin oluşturulma zamanı. Sadece oluşturulma zamanı yeterli.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
