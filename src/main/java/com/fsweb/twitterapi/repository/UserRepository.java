package com.fsweb.twitterapi.repository;

import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository'yi import ediyoruz
import org.springframework.stereotype.Repository; // @Repository anotasyonu için

import java.util.Optional; // find metotları Optional döneceği için
import java.util.UUID; // User ID tipi UUID olduğu için


//Bu arayüz, UUser entity'si için veritabanı işlemlerini yönetecek.


@Repository // Bu arayüzün bir Spring Data Repository'si olduğunu ve Spring'in bean yönetiminde tanınacağını belirtir.
public interface UserRepository extends JpaRepository<User, UUID> {
    // JpaRepository, <Entity Tipi, ID Tipi> parametrelerini alır.
    // Bu arayüz, User entity'si üzerinde findAll, findById, save, delete gibi temel CRUD metotlarını otomatik olarak sağlar.

    // Özel Sorgu Metotları: Spring Data JPA, metot isimlerine göre sorgular oluşturabilir.

    // Username'e göre kullanıcı bulma
    // Optional kullanmak, sonuç bulunmadığında null pointer exception almamızı engeller.
    Optional<User> findByUsername(String username);

    // Email'e göre kullanıcı bulma
    Optional<User> findByEmail(String email);

    // Username veya Email'e göre kullanıcı bulma
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Username'in veritabanında var olup olmadığını kontrol etme
    Boolean existsByUsername(String username);

    // Email'in veritabanında var olup olmadığını kontrol etme
    Boolean existsByEmail(String email);
}