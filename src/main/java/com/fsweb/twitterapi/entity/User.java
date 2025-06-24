package com.fsweb.twitterapi.entity;

import jakarta.persistence.*; // JPA (Java Persistence API) anotasyonları için
import jakarta.validation.constraints.Email; // E-posta validasyonu için
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
import java.util.HashSet; // Set koleksiyonu için (One-to-Many ilişkilerde kullanılacak)
import java.util.Set; // Set arayüzü için (One-to-Many ilişkilerde kullanılacak)

@Entity // Bu sınıfın bir JPA Entity'si olduğunu belirtir, yani bir veritabanı tablosuna karşılık gelir.
@Table(name = "users") // Veritabanındaki tablo adını belirtir. "user" PostgreSQL'de ayrılmış bir kelime olabileceğinden "users" kullanmak daha güvenlidir.
@Data // Lombok anotasyonu: Getter, Setter, equals(), hashCode() ve toString() metodlarını otomatik olarak oluşturur. Kod tekrarını azaltır.
@NoArgsConstructor // Lombok anotasyonu: Argümansız (varsayılan) bir constructor oluşturur. JPA için gereklidir.
@AllArgsConstructor // Lombok anotasyonu: Sınıftaki tüm alanları içeren bir constructor oluşturur.
@Builder // Lombok anotasyonu: Sınıf için Builder deseni sağlar. Nesne oluşturmayı daha okunaklı hale getirir.
public class User {

    @Id // Bu alanın birincil anahtar (Primary Key - PK) olduğunu belirtir. Her tablo için benzersiz bir tanımlayıcıdır.
    @GeneratedValue(generator = "UUID") // ID'nin otomatik olarak nasıl oluşturulacağını belirtir. "UUID" adında bir generator kullanacağız.
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator") // UUID'leri oluşturmak için Hibernate'in UUIDGenerator'ını kullanacağımızı tanımlar.
    @Column(name = "id", updatable = false, nullable = false) // Veritabanındaki sütun adını, güncellenip güncellenemeyeceğini ve boş geçilip geçilemeyeceğini belirtir. `updatable = false` ID'nin bir kez belirlendikten sonra değişmeyeceğini ifade eder. `nullable = false` boş olamayacağını belirtir.
    private UUID id; // Kullanıcı ID'si, benzersiz bir tanımlayıcı (UUID).

    @Column(name = "username", unique = true, nullable = false) // `unique = true` bu sütundaki değerlerin tekrar edemeyeceğini (benzersiz olacağını) belirtir.
    @NotBlank(message = "Username cannot be empty") // Validasyon: Kullanıcı adının boş veya sadece boşluklardan oluşamayacağını garanti eder.
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters") // Validasyon: Kullanıcı adının uzunluğunu belirler.
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid") // Validasyon: E-posta formatının geçerli olmasını sağlar.
    private String email;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password cannot be empty")
    private String password; // Dikkat: Bu şifreler veritabanına kaydedilmeden önce kesinlikle hash'lenmelidir! Güvenlik için kritik.

    @Column(name = "name")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name; // Kullanıcının adı (gerçek adı), opsiyonel.

    @Column(name = "surname")
    @Size(max = 50, message = "Surname cannot exceed 50 characters")
    private String surname; // Kullanıcının soyadı, opsiyonel.

    @Column(name = "bio", length = 255) // Kullanıcının biyografisi için maksimum 255 karakter uzunluk.
    private String bio;

    @Column(name = "profile_image_url") // Profil fotoğrafının URL'si, opsiyonel.
    private String profileImageUrl;

    @CreationTimestamp // Hibernate anotasyonu: Kaydın veritabanına ilk kaydedildiği zamanı otomatik olarak atar.
    @Column(name = "created_at", nullable = false, updatable = false) // `updatable = false` bu zamanın daha sonra değişmeyeceğini belirtir.
    private LocalDateTime createdAt;

    @UpdateTimestamp // Hibernate anotasyonu: Kayıt her güncellendiğinde bu zamanı otomatik olarak günceller.
    @Column(name = "updated_at") // Opsiyonel, sadece güncellendiğinde değeri olur.
    private LocalDateTime updatedAt;

    // --- One-to-Many Relationships ---
    // Bir kullanıcının attığı tweetler
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // `mappedBy = "user"`: Bu ilişkinin Tweet sınıfındaki `user` alanıyla eşleştiğini belirtir.
    // `cascade = CascadeType.ALL`: Bir User silindiğinde ona ait tüm Tweet, Comment, Like, Retweet'lerin de silinmesini sağlar.
    // `orphanRemoval = true`: Bir koleksiyondan (örn. tweets) bir entity kaldırıldığında, o entity'nin veritabanından da silinmesini sağlar.
    private Set<Tweet> tweets = new HashSet<>(); // Boş bir HashSet ile başlatmak iyi bir pratiktir.

    // Bir kullanıcının yaptığı yorumlar
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    // Bir kullanıcının attığı beğeniler
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    // Bir kullanıcının yaptığı retweetler
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Retweet> retweets = new HashSet<>();

    // TODO: Spring Security için kullanıcı rolleri (daha sonra eklenecek)
    /*
    @ElementCollection(fetch = FetchType.EAGER) // Rolleri hemen yükle
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // user_roles tablosunu ve ilişkisini belirt
    @Column(name = "role") // Rol değerinin saklanacağı sütun adı
    private Set<String> roles = new HashSet<>();
    */
}