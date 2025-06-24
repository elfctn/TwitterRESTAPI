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
import java.util.HashSet; // Set koleksiyonu için
import java.util.Set; // Set arayüzü için
import java.util.UUID; // Benzersiz ID (UUID) tipi için

@Entity // Bu sınıfın bir JPA Entity'si olduğunu belirtir, yani bir veritabanı tablosuna karşılık gelir.
@Table(name = "tweets") // Veritabanındaki tablo adını belirtir.
@Data // Lombok anotasyonu: Getter, Setter, equals(), hashCode() ve toString() metodlarını otomatik olarak oluşturur.
@NoArgsConstructor // Lombok anotasyonu: Argümansız (varsayılan) bir constructor oluşturur.
@AllArgsConstructor // Lombok anotasyonu: Sınıftaki tüm alanları içeren bir constructor oluşturur.
@Builder // Lombok anotasyonu: Sınıf için Builder deseni sağlar.
public class Tweet {

    @Id // Birincil anahtar
    @GeneratedValue(generator = "UUID") // ID'nin UUID olarak otomatik üretileceğini belirtir.
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Tweet ID'si

    @ManyToOne(fetch = FetchType.LAZY) // Bir Tweet'in sadece bir Kullanıcısı (Tweet Sahibi) vardır. Many (Tweet) to One (User) ilişkisi.
    @JoinColumn(name = "user_id", nullable = false) // `user_id` sütunu bu tablonun `Tweet` sahibi olan `User` tablosunun ID'sine referans verdiğini belirtir. `nullable = false` çünkü anonim tweet olamaz.
    private User user; // Tweet'in sahibi olan User nesnesi.

    @Column(name = "content", nullable = false, length = 280) // Tweet içeriği, boş olamaz ve maksimum 280 karakter.
    @NotBlank(message = "Tweet content cannot be empty") // Validasyon: İçerik boş olamaz.
    @Size(max = 280, message = "Tweet content cannot exceed 280 characters") // Validasyon: Maksimum karakter sayısı.
    private String content;

    @CreationTimestamp // Kaydın oluşturulma zamanı
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Kaydın son güncellenme zamanı
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Self-referencing ManyToOne: Bir tweet başka bir tweete yanıt olabilir.
    // Senaryo: Kullanıcı A bir tweet attı (Tweet a). Kullanıcı B, bu tweete yanıt (reply) verdi (Tweet b).
    @ManyToOne(fetch = FetchType.LAZY) // Bir yanıt tweeti sadece bir orijinal tweete bağlanır.
    @JoinColumn(name = "reply_to_tweet_id") // Yanıt verilen Tweet'in ID'sine referans veren sütun. Nullable olabilir çünkü her tweet bir yanıt değildir.
    private Tweet replyToTweet; // Yanıt verilen orijinal tweet nesnesi.

    @Column(name = "is_retweet", nullable = false)
    private Boolean isRetweet = false; // Bu tweet'in bir retweet olup olmadığını belirtir. Varsayılan olarak `false`.

    // Self-referencing ManyToOne: bir orijinal tweet bir sürü retweet alabilir.
    // Senaryo: Kullanıcı A bir tweet attı (Tweet a). Kullanıcı B, bu tweeti retweet etti (Tweet b). Kullanıcı C de tweet b yi retweet etti (Tweet c)...
    @ManyToOne(fetch = FetchType.LAZY) // Bir retweet sadece bir orijinal tweete bağlanır.
    @JoinColumn(name = "original_tweet_id") // Retweet edilen orijinal Tweet'in ID'sine referans veren sütun. Nullable olabilir çünkü her tweet bir retweet değildir.
    private Tweet originalTweet; // Retweet edilen orijinal tweet nesnesi.


    // --- One-to-Many Relationships ---

    // Bu tweete yapılan yorumlar
    @OneToMany(mappedBy = "tweet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    // Bu tweete atılan beğeniler
    @OneToMany(mappedBy = "tweet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    // Bu tweet'in retweetleri (bu tweet orijinal ise)
    @OneToMany(mappedBy = "originalTweet", cascade = CascadeType.ALL, orphanRemoval = true)
    // `mappedBy = "originalTweet"`: Retweet sınıfındaki `originalTweet` alanıyla eşleştiğini belirtir.
    private Set<Retweet> retweets = new HashSet<>();

    // Bu tweete verilen yanıtlar (yani bu tweet'e `replyToTweet` ile referans veren diğer tweetler)
    @OneToMany(mappedBy = "replyToTweet", cascade = CascadeType.ALL, orphanRemoval = true)
    // `mappedBy = "replyToTweet"`: Tweet sınıfının kendisindeki `replyToTweet` alanıyla eşleştiğini belirtir.
    private Set<Tweet> replies = new HashSet<>();
}