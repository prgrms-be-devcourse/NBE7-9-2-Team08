package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.RepositoryLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryLanguageRepository extends JpaRepository<RepositoryLanguage, Long> {
    List<RepositoryLanguage> findByRepositories_Id(Long repositoriesId);
}
