package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryJpaRepository extends JpaRepository<Repositories, Long> {
    Optional<Repositories> findByHtmlUrl(String htmlUrl);
}
