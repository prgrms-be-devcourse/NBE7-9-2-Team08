package com.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ========== 공통 에러 ==========
    VALIDATION_FAILED("CMN001", HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
    INTERNAL_ERROR("CMN002", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE("CMN003", HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE("CMN004", HttpStatus.BAD_REQUEST, "잘못된 타입의 값입니다."),
    MISSING_REQUEST_PARAMETER("CMN005", HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),

    // ========== user 도메인 에러 ==========
    LOGIN_FAILED("U001", HttpStatus.BAD_REQUEST, "로그인에 실패했습니다."),
    EMAIL_VERIFY_FAILED("U002", HttpStatus.BAD_REQUEST, "이메일 인증코드가 일치하지 않습니다"),
    NAME_NOT_FOUND("U003", HttpStatus.NOT_FOUND, "이름이 입력되지 않았습니다."),
    PASSWORD_NOT_FOUND("U004", HttpStatus.NOT_FOUND, "비밀번호가 입력되지 않았습니다."),
    PASSWORD_NOT_EQUAL("U005", HttpStatus.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다."),
    EMAIL_NOT_FOUND("U006", HttpStatus.NOT_FOUND, "해당 이메일은 없는 계정입니다.") ,
    ALREADY_REGISTERED_EMAIL("U007", HttpStatus.BAD_REQUEST, "이미 회원가입된 이메일입니다."),

    // ========== analysis 도메인 에러 ==========
    INVALID_GITHUB_URL("A001", HttpStatus.BAD_REQUEST, "올바른 GitHub 저장소 URL이 아닙니다."),
    INVALID_REPOSITORY_PATH("A002", HttpStatus.BAD_REQUEST, "저장소 URL 형식이 잘못되었습니다. 예: https://github.com/{owner}/{repo}"),
    ANALYSIS_NOT_FOUND("A003", HttpStatus.BAD_REQUEST, "분석 결과를 찾을 수 없습니다."),
    USER_NOT_FOUND("A004", HttpStatus.FORBIDDEN, "사용자 정보를 찾을 수 없습니다."),
    FORBIDDEN("A005", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // ========== repository 도메인 에러 ==========
    GITHUB_REPO_NOT_FOUND("G001", HttpStatus.BAD_REQUEST, "GitHub 저장소를 찾을 수 없습니다."),
    GITHUB_API_SERVER_ERROR("G002", HttpStatus.INTERNAL_SERVER_ERROR, "GitHub API 서버에서 오류가 발생했습니다."),
    GITHUB_RATE_LIMIT_EXCEEDED("G003", HttpStatus.TOO_MANY_REQUESTS, "GitHub API 호출 제한을 초과했습니다."),
    GITHUB_INVALID_TOKEN("G004", HttpStatus.UNAUTHORIZED, "GitHub 인증 토큰이 유효하지 않습니다."),
    GITHUB_RESPONSE_PARSE_ERROR("G005", HttpStatus.INTERNAL_SERVER_ERROR, "GitHub 응답 데이터를 처리하는 중 오류가 발생했습니다."),
    GITHUB_API_FAILED("G006", HttpStatus.BAD_REQUEST, "GitHub API 응답에 실패했습니다."),

    // ========== comment 도메인 에러 ==========
    COMMENT_NOT_FOUND("R001", HttpStatus.BAD_REQUEST, "댓글을 찾을 수 없습니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
