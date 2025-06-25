package com.fsweb.twitterapi.dto.user;

import jakarta.validation.constraints.Email; // E-posta validasyonu için (eğer güncellenecekse)
import jakarta.validation.constraints.Size; // Uzunluk validasyonu için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları alan constructor için
import lombok.Builder; // Lombok: Builder deseni için
import lombok.Data; // Lombok: Getter, Setter, toString, equals, hashCode için
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor için

@Data // Getter, Setter, equals(), hashCode(), toString() metodlarını otomatik oluşturur.
@NoArgsConstructor // Argümansız constructor oluşturur.
@AllArgsConstructor // Tüm alanları alan constructor oluşturur.
@Builder // Builder deseni oluşturur.
public class UserUpdateRequest {

    // Güncellenebilecek alanlar şimdilik sadece
    // isim, soyisim, biyografi ve profil resmini güncellenebilir varsayacağım.


    //DAHA SONRA GELİŞTİRMEK İÇİN;;;;
    // Eğer kullanıcı username/email de güncelleyecekse,
    // validasyonlar yeniden eklemeliyim ve service katmanında çakışma kontrolü yapmalıyım .

    // Şifre güncelleme genellikle ayrı bir endpoint veya DTO ile yapılır.
    // Username veya Email gibi benzersiz alanların güncellenmesi özel iş mantığı gerektirir.

    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name;

    @Size(max = 50, message = "Surname cannot exceed 50 characters")
    private String surname;

    @Size(max = 255, message = "Bio cannot exceed 255 characters")
    private String bio;

    private String profileImageUrl;


}