package com.backend.domain.user.controller;

import com.backend.domain.user.dto.LoginResponse;
import com.backend.domain.user.dto.UserDto;
import com.backend.domain.user.service.EmailService;
import com.backend.domain.user.service.JwtService;
import com.backend.domain.user.util.JwtUtil;
import com.backend.domain.user.service.UserService;
import com.backend.global.exception.ErrorCode;
import com.backend.global.response.ApiResponse;
import com.backend.global.response.ResponseCode;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserService userService;
    private final JwtUtil jwtUtil;

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

    /**
     * 인증코드 검증
     * @param email
     * @param code
     */

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
            return ApiResponse.error(ErrorCode.Email_verify_Failed);
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
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ){
        String token = jwtService.login(loginRequest.email, loginRequest.password);
        if (token == null) {
            // ★ 에러 분기도 LoginResponse로 타입을 고정해서 반환
            return ApiResponse.<LoginResponse>error(ResponseCode.UNAUTHORIZED);
        }

        if(token != null) {
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true); // JavaScript 접근 방지 (XSS 공격 방어)
            cookie.setSecure(true); //HTTPS 통신에서만 전송
            cookie.setPath("/");

            cookie.setMaxAge(tokenValidityMilliSeconds);

        response.addCookie(cookie); //응답에 쿠키 추가
        var user = userService.findByEmail(loginRequest.email);


            return ApiResponse.success(new LoginResponse(new UserDto(user)));
        }else{
            return ApiResponse.error(ErrorCode.Login_Failed);
        }
    }

    /**
     * 로그아웃
     */

    @PostMapping("/api/logout")
    public ApiResponse<String> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        //쿠키 만료 명령
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");

        cookie.setMaxAge(0);

        response.addCookie(cookie);

        //redis에 블랙리스트로 등록
        String jwtToken = getJwtToken(request);
        if(jwtToken != null) {
            long expiration = jwtUtil.getExpiration(jwtToken);
            if(expiration > 0) {
                jwtService.logout(jwtToken, expiration);
            }

        }


        return ApiResponse.success("success");
    }


    public String getJwtToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies(); //
        if(cookies != null) {
            for(Cookie c : cookies) {
                if(c.getName().equals("token")) {
                    return c.getValue();
                }
            }
        }
        return null;
    }



}
