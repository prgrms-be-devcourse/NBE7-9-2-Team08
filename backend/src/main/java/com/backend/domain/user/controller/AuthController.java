package com.backend.domain.user.controller;

import com.backend.domain.user.service.EmailService;
import com.backend.global.exception.ErrorCode;
import com.backend.global.response.ApiResponse;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final EmailService emailService;


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

}
