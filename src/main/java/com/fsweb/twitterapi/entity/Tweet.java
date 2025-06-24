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
import java.util.HashSet; // Set koleksiyonu için (ileride kullanılacak)
import java.util.Set; // Set arayüzü için (ileride kullanılacak)

@Entity // Bu sınıfın bir JPA Entity'si olduğunu belirtir, yani bir veritabanı tablosuna karşılık gelir.
@Table(name = "tweets") // Veritabanındaki tablo adını belirtir.
@Data // Lombok anotasyonu: Getter, Setter, equals(), hashCode() ve toString() metodlarını otomatik olarak oluşturur.
@NoArgsConstructor // Lombok anotasyonu: Argümansız (varsayılan) bir constructor oluşturur.
@AllArgsConstructor // Lombok anotasyonu: Sınıftaki tüm alanları içeren bir constructor oluşturur.
@Builder // Lombok anotasyonu: Sınıf için Builder deseni sağlar.
public class Tweet {

    @Id // Birincil anahtar
    @GeneratedValue(generator = "UUID") // UUID generator kullan
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Tweet ID'si

    @ManyToOne(fetch = FetchType.LAZY) // bir kullanıcı birden çok tweet atar. Many (Tweet) to One (User) ilişkisi.
    // `fetch = FetchType.LAZY` demek, User nesnesinin sadece gerçekten ihtiyaç duyulduğunda veritabanından yüklenmesi anlamına gelir. Bu performansı artırır.
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

    // Self-referencing ManyToOne: Bir tweet bir sürü reply tweet alabilir
    //Senaryo: Kullanıcı A bir tweet attı (Tweet a). Kullanıcı B, bu tweete yanıt (reply) verdi (Tweet b).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_tweet_id") // Bu sütun, yanıt verilen Tweet'in ID'sine referans verir. Nullable olabilir çünkü her tweet bir yanıt değildir.
    private Tweet replyToTweet; // Yanıt verilen orijinal tweet nesnesi.



    @Column(name = "is_retweet", nullable = false)
    private Boolean isRetweet = false; // Bu tweet'in bir retweet olup olmadığını belirtir. Varsayılan olarak `false`.

    // Self-referencing ManyToOne: bir orjinal tweet bir sürü rt alabilir
    //Senaryo: Kullanıcı A bir tweet attı (Tweet a). Kullanıcı B, bu tweeti retweet etti (Tweet b). Kullanıcı C de tweet b yi retweet etti (Tweet c)...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_tweet_id") // Bu sütun, retweet edilen orijinal Tweet'in ID'sine referans verir. Nullable olabilir çünkü her tweet bir retweet değildir.
    private Tweet originalTweet; // Retweet edilen orijinal tweet nesnesi.


    // TODO: One-to-Many Relationships (İleride eklenecek, şimdilik yorum satırı) ---
    // Bu kısım, bir tweet'in birden fazla yorumu, beğenisi veya retweet'i olabileceğini belirtir.
    // İlgili diğer Entity'leri oluşturduğumda bu ilişkileri buraya ekleyeceğim.

    @OneToMany(mappedBy = "tweet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "tweet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "originalTweet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Retweet> retweets = new HashSet<>();

    // Bu tweete verilen yanıtlar (yani bu tweet'e `replyToTweetId` ile referans veren diğer tweetler)
    @OneToMany(mappedBy = "replyToTweet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tweet> replies = new HashSet<>();

}