package com.backend.domain.user.controller;

import com.backend.domain.user.service.EmailService;
import com.backend.domain.user.service.JwtService;
import com.backend.global.exception.ErrorCode;
import com.backend.global.response.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final EmailService emailService;
    private final JwtService jwtService;

    @Value("${jwt.access-token-expiration-in-milliseconds}")
    private int tokenValidityMilliSeconds;

    /**
     * 입력받은 이메일에 인증코드를 보냅니다.
     * @param email
     * @throws MessagingException
     */

    record SendRequest(
            String email
    ){

    }

    @PostMapping("/api/auth")
    public ApiResponse<String> sendAuthCode(@RequestBody SendRequest sendRequest) throws MessagingException {
        emailService.sendEmail(sendRequest.email());
        return  ApiResponse.success("이메일 인증 코드 발송 성공");
    }


    record VerifyRequest(
            String email,
            String code
    ){

    }

    @PostMapping("/api/verify")
    public ApiResponse<String> verifyAuthCode(@RequestBody VerifyRequest request)
    {
        if(emailService.verifyAuthCode(request.email(), request.code)){
            //인증 성공시
            return ApiResponse.success("이메일 인증 성공");
        }else{
            return ApiResponse.error(ErrorCode.VALIDATION_FAILED);
        }

    }


    /**
     * 로그인
     */
    record LoginRequest(
            @NotBlank(message = "이메일은 필수 입력값 입니다.")
            @Email(message = "이메일 형식이 아닙니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수 입력값 입니다.")
            String password
    ){

    }



    @PostMapping("/api/login")
    public ApiResponse<String> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ){
        String token = jwtService.login(loginRequest.email, loginRequest.password);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true); // JavaScript 접근 방지 (XSS 공격 방어)
        cookie.setSecure(true); //HTTPS 통신에서만 전송
        cookie.setPath("/");

        cookie.setMaxAge(tokenValidityMilliSeconds);

        response.addCookie(cookie); //응답에 쿠키 추가

        return ApiResponse.success("success");
    }
}
