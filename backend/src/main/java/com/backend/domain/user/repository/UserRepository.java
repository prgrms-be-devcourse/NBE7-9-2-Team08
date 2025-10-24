package com.backend.domain.user.repository;

import com.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = true")
    Optional<User> findByEmailIncludeDeleted(@Param("email") String email);

    boolean existsByEmail(String email);
}
