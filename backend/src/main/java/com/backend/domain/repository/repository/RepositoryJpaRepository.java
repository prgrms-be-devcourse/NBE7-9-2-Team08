package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.Repositories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryJpaRepository extends JpaRepository<Repositories, Long> {
    Optional<Repositories> findByHtmlUrl(String htmlUrl);

    Optional<Repositories> findByHtmlUrlAndUserId(String htmlUrl, Long userId);

    List<Repositories> findByUserId(Long userId);

    // List<Repositories> findByPublicRepository(boolean b);
    Page<Repositories> findByPublicRepositoryTrue(Pageable pageable);
}
