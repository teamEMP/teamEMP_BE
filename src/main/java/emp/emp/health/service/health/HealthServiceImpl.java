package emp.emp.health.service.health;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import emp.emp.auth.exception.AuthErrorCode;
import emp.emp.exception.BusinessException;
import emp.emp.health.dto.request.HealthRecordReq;
import emp.emp.health.dto.response.HealthRecordGraphRes;
import emp.emp.health.dto.response.HealthRecordRes;
import emp.emp.health.entity.Health;
import emp.emp.health.enums.Type;
import emp.emp.health.exception.HealthErrorCode;
import emp.emp.health.repository.HealthRepository;
import emp.emp.health.service.comment.HealthCommentService;
import emp.emp.member.entity.Member;
import emp.emp.member.repository.MemberRepository;
import emp.emp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

	private final SecurityUtil securityUtil;
	private final HealthCommentService healthCommentService;
	private final HealthRepository healthRepository;
	private final MemberRepository memberRepository;

	/**
	 * 건강 기록 등록 메서드
	 *
	 * @param request 타입, 수치
	 */
	@Override
	@Transactional
	public void recordHealth(HealthRecordReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		Type type = validateType(request.getType());

		validateRecord(currentMember, type);

		Health healthRecord = Health.builder()
			.member(currentMember)
			.type(type)
			.value(request.getValue())
			.build();

		healthRepository.save(healthRecord);
	}

	/**
	 * 오늘 이미 등록된 같은 타입의 건강 기록이 있는지 검사하는 메서드
	 *
	 * @param member 로그인한 유저
	 * @param type   타입
	 */
	private void validateRecord(Member member, Type type) {
		LocalDate today = LocalDate.now();

		LocalDateTime startOfToday = today.atStartOfDay();
		LocalDateTime endOfToday = today.plusDays(1).atStartOfDay().minusNanos(1);

		boolean exists = healthRepository.existsByMemberAndTypeAndCreatedAtBetween(member, type, startOfToday,
			endOfToday);

		if (exists) {
			throw new BusinessException(HealthErrorCode.ALREADY_RECORD_TODAY);
		}
	}

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
	@Override
	public HealthRecordRes getWeeklyHealthRecords(String verifyId, int year, int month, int week,
		String inputType) {

		Member member = findMemberByVerifyId(verifyId);
		Type type = validateType(inputType);

		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate firstMonday = startOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
		LocalDate weekStart = firstMonday.plusWeeks(week - 1);
		LocalDate weekEnd = weekStart.plusDays(6);

		LocalDateTime startDateTime = weekStart.atStartOfDay();
		LocalDateTime endDateTime = weekEnd.atTime(LocalTime.MAX);

		List<HealthRecordGraphRes> healthRecords = getHealthRecordGraphValues(member, type, startDateTime, endDateTime);

		String aiComment = healthCommentService.getAiComment(year, month, week, type, healthRecords);

		return getHealthRecords(healthRecords, aiComment);
	}

	/**
	 * 월간 건강 기록 조회 메서드
	 *
	 * @param verifyId  조회 대상 회원의 아이디
	 * @param year      조회할 연도
	 * @param month     조회할 월
	 * @param inputType 건강 기록 타입
	 * @return 해당 월에 해당하는 건강 기록 리스트 + AI 요약
	 */
	@Override
	public HealthRecordRes getMonthlyHealthRecords(String verifyId, int year, int month, String inputType) {

		Member member = findMemberByVerifyId(verifyId);
		Type type = validateType(inputType);

		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

		LocalDateTime startDateTime = startOfMonth.atStartOfDay();
		LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

		List<HealthRecordGraphRes> healthRecords = getHealthRecordGraphValues(member, type, startDateTime, endDateTime);

		String aiComment = healthCommentService.getAiComment(year, month, type, healthRecords);

		return getHealthRecords(healthRecords, aiComment);
	}

	/**
	 * verifyId를 통해 회원을 조회하는 공통 메서드
	 */
	private Member findMemberByVerifyId(String verifyId) {
		return memberRepository.findByVerifyId(verifyId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
	}

	/**
	 * 타입 검증
	 *
	 * @param type 타입
	 */
	private Type validateType(String type) {
		try {
			return Type.valueOf(type);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(HealthErrorCode.TYPE_ERROR);
		}
	}

	/**
	 * 지정한 회원, 타입, 날짜 범위에 해당하는 건강 기록들을 조회 후 DTO로 변환하는 공통 메서드
	 */
	private List<HealthRecordGraphRes> getHealthRecordGraphValues(Member member, Type type, LocalDateTime startDateTime,
		LocalDateTime endDateTime) {

		List<Health> healthList = healthRepository.findByMemberAndTypeAndCreatedAtBetween(
			member, type, startDateTime, endDateTime).orElse(Collections.emptyList());

		return healthList.stream()
			.map(health -> HealthRecordGraphRes.builder()
				.value(health.getValue())
				.date(health.getCreatedAt().toLocalDate())
				.build())
			.toList();
	}

	/**
	 * 그래프를 위한 수치와 AI 통계 요약을 담은 DTO로 변환하는 메서드
	 *
	 * @param graphRes  수치
	 * @param aiComment 통계 요약
	 * @return 주간/월간 건강 정보
	 */
	private HealthRecordRes getHealthRecords(List<HealthRecordGraphRes> graphRes, String aiComment) {
		return HealthRecordRes.builder()
			.healthComment(aiComment)
			.recordGraphRes(graphRes)
			.build();
	}
}
