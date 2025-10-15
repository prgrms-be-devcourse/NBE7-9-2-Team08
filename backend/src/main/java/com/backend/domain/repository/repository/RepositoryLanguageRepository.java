package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.RepositoryLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositoryLanguageRepository extends JpaRepository <RepositoryLanguage, Long>{
}
