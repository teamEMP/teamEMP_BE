package emp.emp.health.exception;

import org.springframework.http.HttpStatus;

import emp.emp.util.api_response.error_code.ErrorCode;
import lombok.Getter;

@Getter
public enum HealthErrorCode implements ErrorCode {

	TYPE_ERROR("Heal-001", HttpStatus.BAD_REQUEST, "잘못된 건강 타입"),
	ALREADY_RECORD_TODAY("Heal-001", HttpStatus.BAD_REQUEST, "이미 오늘의 건강 기록을 등록했습니다");

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

	HealthErrorCode(String code, HttpStatus httpStatus, String message) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
