package com.backend.domain.repository.controller;

import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repository")
public class RepositoriseController {
    private final RepositoryService repositoryService;

    @GetMapping("/delete/{repotiroiesId}")
    public void deleteRepository(@PathVariable Long repositoriesId){
        repositoryService.delete(repositoriesId);
    }
}
