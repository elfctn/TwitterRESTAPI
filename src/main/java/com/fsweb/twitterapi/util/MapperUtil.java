package com.fsweb.twitterapi.util;

// Bu sınıf genellikle Entity ve DTO dönüşümleri için kullanılır.
// Şu an için dönüşümleri doğrudan Service katmanında yapıyoruz (örn: mapUserToUserResponse).
// Daha sonra, eğer kod tekrarı artarsa veya ModelMapper/MapStruct gibi bir kütüphane eklenirse,
// bu sınıf bu dönüşüm mantığını merkezileştirmek için kullanılabilir.

public class MapperUtil {

    // Örnek: Bir DTO'dan Entity'ye dönüşüm metodu (şimdilik boş)
    /*
    public static User toUserEntity(UserRegisterRequest dto) {
        // Dönüşüm mantığı buraya gelecek
        return null; // Örnek dönüş
    }
    */

    // Örnek: Bir Entity'den DTO'ya dönüşüm metodu (şimdilik boş)
    /*
    public static UserResponse toUserResponseDto(User entity) {
        // Dönüşüm mantığı buraya gelecek
        return null; // Örnek dönüş
    }
    */

    // Bu sınıf, statik yardımcı metotlar içerdiği için bir instance'ına ihtiyaç duyulmaz.
    private MapperUtil() {
        // Constructor'ı private yaparak dışarıdan nesne oluşturulmasını engelleriz.
    }
}
