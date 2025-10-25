package com.backend.domain.user.service;

import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.util.RedisUtil;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;


    public User join(@NotBlank String email, @NotBlank String password, @NotBlank String passwordCheck, @NotBlank String name) throws MessagingException {

        //email이 인증을 통과했는지 검증
        String verified = redisUtil.getData("VERIFIED_EMAIL:" + email);
        if(verified == null){
            System.out.println("이메일 인증을 받지않은 이메일입니다.");
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }



        //email중복검증
        if(userRepository.findByEmail(email).isPresent()){
            System.out.println("이미 등록된 이메일입니다.");
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        //passwordCheck 검증
        if(!(password.equals(passwordCheck))){
            System.out.println("비밀번호 확인이 비밀번호와 같지않습니다.");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }


        //검증이 완료되었다면 해당 email을 redis에서 삭제
        if(redisUtil.deleteData("VERIFIED_EMAIL:" + email)) {
            System.out.println("VERIFIED_EMAIL:" + email + "은 email검증이 완료되어서 redis에서 삭제가 됐습니다.");
        }else{
            System.out.println("redis 삭제 실패입니다.");
        }


        //암호화 된 비밀번호를 저장
        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(email, encodedPassword, name);
        return userRepository.save(user);


    }

    @Transactional(readOnly = true)
    public User findByEmail(@NotBlank String email) {
        if(userRepository.findByEmail(email).isPresent()){
            return userRepository.findByEmail(email).get();
        }else{
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

    }

    @Transactional(readOnly = true)
    public List<User> findByAll() {
        return userRepository.findAll();
    }

    public String createCode(){
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));
        return code;
    }

    public User softdelete(@NotBlank String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        System.out.println("user : " +user.getEmail());
        System.out.println("delete호출");
        if(user != null){
            user.delete();
            System.out.println("user : " +user.getDeleteDate());
            return user;
        }else{
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
    }

    public User restore(@NotBlank String email) {
        System.out.println("email : " + email);
        User user = userRepository.findByEmailIncludeDeleted(email).orElse(null);
        if(user != null){
            user.restore();
            System.out.println("user restore ");
            return user;
        }else{
            System.out.println("restore 실패");
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
    }

    public User modifyName(String email, @NotBlank(message = "이름은 필수 입력값 입니다.") String name) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED));
        user.changeName(name);
        return user;

    }

    public User modifyPassword(String email, @NotBlank(message = "비밀번호는 필수 입력값 입니다.") String password, String passwordCheck) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED));

        if(password.equals(passwordCheck)){

            user.changePassword(passwordEncoder.encode(password));
        }else{
            throw new BusinessException(ErrorCode.PASSWORD_NOT_EQUAL);
        }


        return user;
    }

    public User getUserNameByUserId(Long userId){
        return userRepository.findNameById(userId);
    }
}
