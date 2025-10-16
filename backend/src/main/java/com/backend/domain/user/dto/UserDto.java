package com.backend.domain.user.dto;

import com.backend.domain.user.entity.User;

public record UserDto (
        Long id,
        String email,
        String name,
        String imageUrl
){
    public UserDto(User user){
        this(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getImageUrl()
        );
    }
}
