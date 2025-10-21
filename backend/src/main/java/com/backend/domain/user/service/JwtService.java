package com.backend.domain.user.service;

import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.util.JwtUtil;
import com.backend.domain.user.util.RedisUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    public String login(@NotBlank(message = "이메일은 필수 입력값 입니다.") @Email(message = "이메일 형식이 아닙니다.") String email, @NotBlank(message = "비밀번호는 필수 입력값 입니다.") String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user.getPassword().equals(password)) {
            //email에 대응하는 비밀번호가 맞다면 jwt토큰 발급
            return jwtUtil.createToken(user.getEmail(), user.getName());
        }else{
            return  null;
        }
    }

    public boolean logout(String token, long expiration) {
        //jtw받아서 redis 블랙리스트에 추가
        String key = "jwt:blacklist:"+token;

        redisUtil.setData(key, "logout", expiration);

        return true;
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     *
     */
    public boolean isBlacklisted(String token){
        String key = "jwt:blacklist:"+token;
        return redisUtil.hasKey(key);
    }
}
