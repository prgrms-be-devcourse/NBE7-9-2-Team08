package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.GitRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GitRepositoryRepository extends JpaRepository <GitRepository, Long>{
    List<GitRepository> findByMember_Id(Long memberId);
}
