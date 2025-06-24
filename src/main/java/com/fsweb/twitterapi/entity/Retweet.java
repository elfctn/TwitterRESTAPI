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
// user_id ve original_tweet_id sütunlarının birleşimi benzersiz olmalıdır.
// Bu, bir kullanıcının aynı tweeti birden fazla kez retweet etmesini engeller.
@Data // Getter, Setter vb. sağlar.
@NoArgsConstructor // Argümansız constructor.
@AllArgsConstructor // Tüm argümanları alan constructor.
@Builder // Builder deseni.
public class Retweet {

    @Id
    @GeneratedValue(generator = "UUID") // ID'nin UUID olarak otomatik üretileceğini belirtir.
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) // Bir retweet sadece bir kullanıcı tarafından yapılır.
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Retweeti yapan kullanıcı nesnesi.

    @ManyToOne(fetch = FetchType.LAZY) // Bir orijinal tweete birden fazla retweet gelebilir.
    @JoinColumn(name = "original_tweet_id", nullable = false)
    private Tweet originalTweet; // Retweet edilen orijinal tweet nesnesi.

    @CreationTimestamp // Retweetin oluşturulma zamanı.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}