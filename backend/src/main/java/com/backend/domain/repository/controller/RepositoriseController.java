package com.backend.domain.repository.controller;

import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repository")
public class RepositoriseController {
    private final RepositoryService repositoryService;

    // 리포지토리 삭제
    @DeleteMapping("/delete/{repositoriesId}")
    public void deleteRepository(@PathVariable("repositoriesId") Long repositoriesId){
        repositoryService.delete(repositoriesId);
    }
}
