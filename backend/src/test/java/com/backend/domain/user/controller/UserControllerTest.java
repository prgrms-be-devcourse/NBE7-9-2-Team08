package com.backend.domain.user.controller;

import com.backend.domain.user.entity.User;
import com.backend.domain.user.service.UserService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @TestConfiguration
    static class MailStubConfig {
        @Bean @Primary
        JavaMailSender javaMailSender() {
            // 모든 send(...)가 doNothing()인 목 객체
            return mock(JavaMailSender.class);
        }
    }

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("회원가입")
    void t1() throws MessagingException { // join(...)이 throws라면 테스트에도 붙여주세요
        String email = "test+" + System.currentTimeMillis() + "@example.com";
        String password = "123456";
        String passwordCheck = "123456";
        String name = "임병수";

        User user = userService.join(email, password, passwordCheck, name);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        // 실제 서비스에서 비밀번호를 암호화하면 아래 단언은 맞지 않을 수 있습니다.
        // assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getName()).isEqualTo(name);
    }
}
