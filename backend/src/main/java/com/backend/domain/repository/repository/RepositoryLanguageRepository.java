package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.RepositoryLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryLanguageRepository extends JpaRepository <RepositoryLanguage, Long>{

    @Query("SELECT rl.language FROM RepositoryLanguage rl WHERE rl.gitRepository.id = :gitRepositoryId")
    List<Language> findLanguagesByGitRepositoryId(@Param("gitRepositoryId") Long gitRepositoryId);
}
