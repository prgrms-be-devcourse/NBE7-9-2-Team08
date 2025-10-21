package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryJpaRepository extends JpaRepository<Repositories, Long> {
    Optional<Repositories> findByHtmlUrl(String htmlUrl);

    List<Repositories> findByUserId(Long userId);

    List<Repositories> findByPublicRepository(boolean b);
}
