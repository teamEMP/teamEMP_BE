package emp.emp.health.service.comment;

import java.util.List;

import emp.emp.health.dto.response.HealthRecordGraphRes;
import emp.emp.health.enums.Type;

public interface HealthCommentService {

	/**
	 * 주간 건강 데이터를 바탕으로 AI 통계 요약 제공
	 *
	 * @param year  연도
	 * @param month 월
	 * @param week  주
	 * @param type  타입
	 * @param data  데이터
	 * @return AI 통계 요약
	 */
	String getAiComment(int year, int month, int week, Type type, List<HealthRecordGraphRes> data);

	/**
	 * 월간 건강 데이터를 바탕으로 AI 통계 요약 제공
	 *
	 * @param year  연도
	 * @param month 월
	 * @param type  타입
	 * @param data  데이터
	 * @return AI 통계 요약
	 */
	String getAiComment(int year, int month, Type type, List<HealthRecordGraphRes> data);
}
