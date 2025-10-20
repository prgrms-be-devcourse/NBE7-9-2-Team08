package com.backend.domain.user.service;

import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.util.JwtUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public String login(@NotBlank(message = "이메일은 필수 입력값 입니다.") @Email(message = "이메일 형식이 아닙니다.") String email, @NotBlank(message = "비밀번호는 필수 입력값 입니다.") String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user.getPassword().equals(password)) {
            //email에 대응하는 비밀번호가 맞다면 jwt토큰 발급
            return jwtUtil.createToken(user.getEmail(), user.getName());
        }else{
            return  null;
        }
    }
}
