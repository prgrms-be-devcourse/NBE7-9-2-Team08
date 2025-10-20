package com.backend.domain.user.controller;

import com.backend.domain.user.dto.UserDto;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.service.UserService;
import com.backend.global.response.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * id를 입력받아 회원이 존재하면 해당 회원을 반환하는 api입니다.
     *
     * @param id
     * @return
     */

    record GetRequest(
            @NotBlank(message = "이메일은 필수 입력값 입니다.")
            @Email(message = "이메일 형식이 아닙니다.")
            String email
    ){

    }

    record GetResponse(
            UserDto userDto
    ){

    }

    @GetMapping("/api/user")
    public ApiResponse<GetResponse> getUser(
            @Valid @RequestBody GetRequest request
    ){
        User user = userService.findByEmail(request.email);
        return ApiResponse.success(new GetResponse(new UserDto(user)));
    }


    /**
     * 모든 회원을 조회하는 api입니다.
     * @param userDtoList
     */


    record GetUsersResponse(
            List<UserDto> userDtoList
    ){

    }


    @GetMapping("/api/users")
    public ApiResponse<GetUsersResponse> getUsers(){
        List<User> users = userService.findByAll();

        List<UserDto> userDtoList = users.stream()
                .map(UserDto::new)
                .toList();

        return ApiResponse.success(new GetUsersResponse(userDtoList));
    }


    /**
     * email, password, passwrodCheck, name을 입력받아 회원가입을 진행하는 api입니다.
     *
     * @param email, password, passwrodCheck, name
     * @return
     */

    record JoinRequest (
            @NotBlank(message = "이메일은 필수 입력값 입니다.")
            @Email(message = "이메일 형식이 아닙니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수 입력값 입니다.")
            String password,

            @NotBlank(message = "비밀번호 확인은 필수 입력값 입니다.")
            String passwordCheck,

            @NotBlank(message = "사용자 이름은 필수 입력값 입니다.")
            String name
    ){

    }

    record JoinResponse (
            UserDto userDto
    ){

    }

    @PostMapping("/api/user")
    public ApiResponse<JoinResponse> join(
            @Valid @RequestBody JoinRequest joinRequest
    ) throws MessagingException {
        User user = userService.join(joinRequest.email, joinRequest.password, joinRequest.passwordCheck, joinRequest.name);

        return ApiResponse.success(new JoinResponse(new UserDto(user)));
    }

    /**
     * soft delete
     */
    record DeleteRequest (
            @NotBlank(message = "이메일은 필수 입력값 입니다.")
            @Email(message = "이메일 형식이 아닙니다.")
            String email
    ){

    }
    record DeleteResponse (
            UserDto userDto
    ){

    }
    @DeleteMapping("/api/user")
    public ApiResponse<DeleteResponse> softDelete(
            @Valid @RequestBody DeleteRequest deleteRequest
    ){
        User softDeleteUser = userService.softdelete(deleteRequest.email);
        return  ApiResponse.success(new DeleteResponse(new UserDto(softDeleteUser)));
    }

    /**
     * 삭제된 유저 복구
     */

    record RestoreRequest (
            @NotBlank(message = "이메일은 필수 입력값 입니다.")
            @Email(message = "이메일 형식이 아닙니다.")
            String email
    ){

    }
    record RestoreResponse (
            UserDto userDto
    ){

    }

    @PostMapping("/api/user/restore")
    public ApiResponse<RestoreResponse> restoreUser(
            @Valid @RequestBody RestoreRequest restoreRequest
    ){
        System.out.println("restore aip 호출");
        User user = userService.restore(restoreRequest.email);
        return ApiResponse.success(new RestoreResponse(new UserDto(user)));
    }

}
