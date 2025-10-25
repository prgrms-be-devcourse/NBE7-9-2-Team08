package com.backend.domain.user.service;

import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.util.RedisUtil;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;

    private static final long EXPIRE_TIME = 300L;
    private static final long JOIN_TIME = 600L;

    /**
     * 인증코드 생성
     *
     */
    private String createCode(){
        Random random = new Random();
        return String.format("%06d",random.nextInt(1000000));
    }

    /**
     * 인증코드를 생성하고 Redis에 저장하며 이메일로 전송합니다.
     * @param email
     */
    public void sendEmail(String email) throws MessagingException {
        //이미 회원가입된 email이면 예외 발생
        boolean existsByEmail = userRepository.existsByEmail(email);
        if(existsByEmail){
            System.out.println("이미 회원가입된 이메일 입니다.");
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        String authCode = createCode();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        // true: 멀티파트 메시지(HTML 등) 활성화, "utf-8": 인코딩 설정
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

        helper.setTo(email);
        helper.setSubject("[임시 서비스 이름] 회원가입 인증 코드입니다.");

        // 이메일 본문 (HTML 형식으로 보냄)
        String content = "<h2>안녕하세요. [PortfolioIQ]입니다.</h2>"
                + "<p>아래 6자리 인증 코드를 인증 창에 입력해 주세요.</p>"
                + "<div style='font-size: 24px; font-weight: bold; color: #1e88e5;'>"
                + authCode
                + "</div>"
                + "<p style='color: #888;'>이 코드는 "+(EXPIRE_TIME/60)+"분 후에 만료됩니다.</p>";

        helper.setText(content, true); // true: HTML 형식 사용 명시

        javaMailSender.send(mimeMessage);

        // Redis에 [이메일]:[인증코드]를 EXPIRE_TIME 동안 저장
        redisUtil.setData(email, authCode, EXPIRE_TIME);
        System.out.println("set data정보 : ");
        System.out.println("email : " + email);
        System.out.println("authCode : " + authCode);
        System.out.println("expireTime : " + EXPIRE_TIME);
    }

    /**
     * Redis에저장된 코드와 사용자가 입력한 코드를 비교합니다.
     * @param email 사용자 이메일 주소
     * @param code 사용자 입력 인증 코드
     * @return 인증 성공 여부
     */
    public boolean verifyAuthCode(String email, String code){
        String storedCode = redisUtil.getData(email);

        if(storedCode == null || !storedCode.equals(code)){
            System.out.println("stored code is : " + storedCode);
            System.out.println("verifyAuthCode메서드이며 storedCode가 없거나 입력값이 잘못됐을경우");
            return false;
        }

        //인증 성공시 해당 redis는 삭제하고
        redisUtil.deleteData(email);

        //해당 이메일은 인증이 성공했다는 플래그를 남김
        redisUtil.setData("VERIFIED_EMAIL:"+email, "Y", JOIN_TIME);
        System.out.println("생성된 플래그");
        System.out.println(redisUtil.getData("VERIFIED_EMAIL:" + email));
        return true;
    }
}
