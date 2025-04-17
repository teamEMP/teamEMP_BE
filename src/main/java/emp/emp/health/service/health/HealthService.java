package emp.emp.health.service.health;

import emp.emp.health.dto.request.HealthRecordReq;
import emp.emp.health.dto.response.HealthRecordRes;

public interface HealthService {

	/**
	 * 건강 기록 등록 메서드
	 *
	 * @param request 타입, 수치
	 */
	void recordHealth(HealthRecordReq request);

	/**
	 * 주간 건강 기록 조회 메서드
	 *
	 * @param verifyId  조회 대상 회원의 아이디
	 * @param year      조회할 연도
	 * @param month     조회할 월
	 * @param week      조회할 주
	 * @param inputType 건강 기록 타입
	 * @return 해당 기간에 해당하는 건강 기록 리스트 + AI 요약
	 */
	HealthRecordRes getWeeklyHealthRecords(String verifyId, int year, int month, int week, String inputType);

	/**
	 * 월간 건강 기록 조회 메서드
	 *
	 * @param verifyId  조회 대상 회원의 아이디
	 * @param year      조회할 연도
	 * @param month     조회할 월
	 * @param inputType 건강 기록 타입
	 * @return 해당 월에 해당하는 건강 기록 리스트 + AI 요약
	 */
	HealthRecordRes getMonthlyHealthRecords(String verifyId, int year, int month, String inputType);
}
