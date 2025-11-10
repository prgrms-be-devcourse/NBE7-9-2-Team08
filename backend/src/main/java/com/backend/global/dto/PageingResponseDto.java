package com.backend.global.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class PageingResponseDto<T> {
    private final List<T> content;      // 실제 데이터 목록
    private final int page;             // 현재 페이지 번호
    private final int size;             // 페이지당 데이터 수
    private final int totalPages;       // 전체 페이지 수
    private final long totalElements;   // 전체 데이터 수
    private final boolean last;         // 마지막 페이지 여부

    public PageingResponseDto(org.springframework.data.domain.Page<T> pageData) {
        this.content = pageData.getContent();
        this.page = pageData.getNumber();
        this.size = pageData.getSize();
        this.totalPages = pageData.getTotalPages();
        this.totalElements = pageData.getTotalElements();
        this.last = pageData.isLast();
    }
}