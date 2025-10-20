package com.backend.domain.community.service;

import com.backend.domain.community.repository.CommentRepository;
import com.backend.domain.repository.entity.Repositories;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final CommentRepository commentRepository;

    // repository 조회 : publicRepository = true
    public List<Repositories> getCommunityRepository(){
        return commentRepository.findByPublicReposiotry(true);
    }

    // repository에 댓글 달기
}
