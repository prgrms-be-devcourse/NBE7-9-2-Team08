package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository <Language, Long>{
}
