package emp.emp.health.service.comment;

import java.util.List;

import org.springframework.stereotype.Service;

import emp.emp.health.dto.response.HealthRecordGraphRes;
import emp.emp.health.enums.Type;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthCommentServiceImpl implements HealthCommentService {

	@Override
	public String getAiComment(int year, int month, int week, Type type, List<HealthRecordGraphRes> data) {

		if (data.isEmpty()) {
			return "";
		}

		return "임시 주간 통계 요약입니다.";
	}

	@Override
	public String getAiComment(int year, int month, Type type, List<HealthRecordGraphRes> data) {

		if (data.isEmpty()) {
			return "";
		}

		return "임시 월간 통계 요약입니다.";
	}
}
