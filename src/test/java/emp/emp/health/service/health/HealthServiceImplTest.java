package emp.emp.health.service.health;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HealthServiceImplTest {

	@Mock
	private SecurityUtil securityUtil;
	@Mock
	private HealthCommentService healthCommentService;
	@Mock
	private HealthRepository healthRepository;
	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private HealthServiceImpl healthService;

	private Member dummyMember;

	@BeforeEach
	void setup() {
		dummyMember = Member.builder()
			.verifyId("dummy123")
			.build();
	}

	@Test
	@DisplayName("건강 기록 등록 - 정상 등록")
	void testRecordHealth_Success() {
		// given: HealthRecordReq를 통한 등록 요청 (타입은 enum의 name 사용)
		HealthRecordReq req = HealthRecordReq.builder()
			.type("BLOOD_PRESSURE")
			.value(120.0)
			.build();

		when(securityUtil.getCurrentMember()).thenReturn(dummyMember);
		when(healthRepository.existsByMemberAndTypeAndCreatedAtBetween(
			eq(dummyMember),
			eq(Type.BLOOD_PRESSURE),
			any(LocalDateTime.class),
			any(LocalDateTime.class)))
			.thenReturn(false);

		// when: 건강 기록 등록 메서드 호출
		healthService.recordHealth(req);

		// then: healthRepository.save가 한 번 호출되어야 함
		verify(healthRepository, times(1)).save(any(Health.class));
	}

	@Test
	@DisplayName("건강 기록 등록 - 이미 등록된 기록이 있는 경우 예외 발생")
	void testRecordHealth_AlreadyRecorded() {
		// given
		HealthRecordReq req = HealthRecordReq.builder()
			.type("BLOOD_PRESSURE")
			.value(120.0)
			.build();

		when(securityUtil.getCurrentMember()).thenReturn(dummyMember);
		// 오늘 이미 해당 타입의 기록이 존재하는 경우
		when(healthRepository.existsByMemberAndTypeAndCreatedAtBetween(
			eq(dummyMember),
			eq(Type.BLOOD_PRESSURE),
			any(LocalDateTime.class),
			any(LocalDateTime.class)))
			.thenReturn(true);

		// when, then: BusinessException이 발생하는지 검증
		assertThatThrownBy(() -> healthService.recordHealth(req))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(HealthErrorCode.ALREADY_RECORD_TODAY.getMessage());
	}

	@Test
	@DisplayName("주간 건강 기록 조회 - 정상 조회")
	void testGetWeeklyHealthRecords() {
		// given
		String verifyId = "dummy123";
		int year = 2025;
		int month = 4;
		int week = 2;
		String inputType = "BLOOD_PRESSURE";

		when(memberRepository.findByVerifyId(verifyId)).thenReturn(Optional.of(dummyMember));

		// 주간 조회를 위한 날짜 계산
		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate firstMonday = startOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
		LocalDate weekStart = firstMonday.plusWeeks(week - 1);
		LocalDate weekEnd = weekStart.plusDays(6);
		LocalDateTime startDateTime = weekStart.atStartOfDay();
		LocalDateTime endDateTime = weekEnd.atTime(LocalTime.MAX);

		// 주간 조회용 Health 레코드 생성 (Health 엔티티의 value는 double로 가정)
		Health health = Health.builder()
			.member(dummyMember)
			.type(Type.BLOOD_PRESSURE)
			.value(120.0)
			.build();
		ReflectionTestUtils.setField(health, "createdAt", LocalDateTime.of(year, month, 10, 10, 0));

		when(healthRepository.findByMemberAndTypeAndCreatedAtBetween(
			eq(dummyMember),
			eq(Type.BLOOD_PRESSURE),
			eq(startDateTime),
			eq(endDateTime)))
			.thenReturn(Optional.of(List.of(health)));

		// AI 통계 요약 반환값 설정
		when(healthCommentService.getAiComment(
			eq(year),
			eq(month),
			eq(week),
			eq(Type.BLOOD_PRESSURE),
			anyList()))
			.thenReturn("주간 통계 요약");

		// when: 주간 건강 기록 조회 메서드 호출
		HealthRecordRes result = healthService.getWeeklyHealthRecords(verifyId, year, month, week, inputType);

		// then: 결과 검증
		assertThat(result).isNotNull();
		assertThat(result.getHealthComment()).isEqualTo("주간 통계 요약");
		assertThat(result.getRecordGraphRes()).hasSize(1);
		HealthRecordGraphRes graphRes = result.getRecordGraphRes().get(0);
		assertThat(graphRes.getValue()).isEqualTo(120.0);
		assertThat(graphRes.getDate()).isEqualTo(health.getCreatedAt().toLocalDate());
	}

	@Test
	@DisplayName("월간 건강 기록 조회 - 정상 조회")
	void testGetMonthlyHealthRecords() {
		// given
		String verifyId = "dummy123";
		int year = 2025;
		int month = 4;
		String inputType = "BLOOD_PRESSURE";

		when(memberRepository.findByVerifyId(verifyId)).thenReturn(Optional.of(dummyMember));

		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
		LocalDateTime startDateTime = startOfMonth.atStartOfDay();
		LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

		// 월간 조회용 Health 레코드 생성
		Health health = Health.builder()
			.member(dummyMember)
			.type(Type.BLOOD_PRESSURE)
			.value(130.0)
			.build();
		ReflectionTestUtils.setField(health, "createdAt", LocalDateTime.of(year, month, 10, 10, 0));

		when(healthRepository.findByMemberAndTypeAndCreatedAtBetween(
			eq(dummyMember),
			eq(Type.BLOOD_PRESSURE),
			eq(startDateTime),
			eq(endDateTime)))
			.thenReturn(Optional.of(List.of(health)));

		// AI 통계 요약 반환값 설정
		when(healthCommentService.getAiComment(
			eq(year),
			eq(month),
			eq(Type.BLOOD_PRESSURE),
			anyList()))
			.thenReturn("월간 통계 요약");

		// when: 월간 건강 기록 조회 메서드 호출
		HealthRecordRes result = healthService.getMonthlyHealthRecords(verifyId, year, month, inputType);

		// then: 결과 검증
		assertThat(result).isNotNull();
		assertThat(result.getHealthComment()).isEqualTo("월간 통계 요약");
		assertThat(result.getRecordGraphRes()).hasSize(1);
		HealthRecordGraphRes graphRes = result.getRecordGraphRes().get(0);
		assertThat(graphRes.getValue()).isEqualTo(130.0);
		assertThat(graphRes.getDate()).isEqualTo(health.getCreatedAt().toLocalDate());
	}

	@Test
	@DisplayName("주간 건강 기록 조회 - 데이터 없음, 빈 결과 반환")
	void testGetWeeklyHealthRecords_NoData() {
		// given
		String verifyId = "dummy123";
		int year = 2025;
		int month = 4;
		int week = 2;
		String inputType = "BLOOD_PRESSURE";

		// 회원 조회
		when(memberRepository.findByVerifyId(verifyId))
			.thenReturn(Optional.of(dummyMember));

		// 주간 조회를 위한 날짜 계산
		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate firstMonday = startOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
		LocalDate weekStart = firstMonday.plusWeeks(week - 1);
		LocalDate weekEnd = weekStart.plusDays(6);
		LocalDateTime startDateTime = weekStart.atStartOfDay();
		LocalDateTime endDateTime = weekEnd.atTime(LocalTime.MAX);

		// DB 조회 결과가 없음을 Optional.of(emptyList())로 설정
		when(healthRepository.findByMemberAndTypeAndCreatedAtBetween(
			eq(dummyMember),
			eq(Type.BLOOD_PRESSURE),
			eq(startDateTime),
			eq(endDateTime)
		)).thenReturn(Optional.of(Collections.emptyList()));

		// healthCommentService의 getAiComment 호출 시, 빈 리스트를 전달하면 빈 문자열을 반환하도록 설정
		when(healthCommentService.getAiComment(
			eq(year),
			eq(month),
			eq(week),
			eq(Type.BLOOD_PRESSURE),
			argThat(list -> list != null && list.isEmpty())
		)).thenReturn("");

		// when: 주간 건강 기록 조회 메서드 호출
		HealthRecordRes result = healthService.getWeeklyHealthRecords(verifyId, year, month, week, inputType);

		// then: 결과는 null이 아니며, recordGraphRes는 빈 리스트, healthComment는 빈 문자열이어야 함
		assertThat(result).isNotNull();
		assertThat(result.getRecordGraphRes()).isEmpty();
		assertThat(result.getHealthComment()).isEqualTo("");
	}

	@Test
	@DisplayName("월간 건강 기록 조회 - 데이터 없음, 빈 결과 반환")
	void testGetMonthlyHealthRecords_NoData() {
		// given
		String verifyId = "dummy123";
		int year = 2025;
		int month = 4;
		String inputType = "BLOOD_PRESSURE";

		// 회원 조회
		when(memberRepository.findByVerifyId(verifyId))
			.thenReturn(Optional.of(dummyMember));

		// 월간 조회를 위한 날짜 계산
		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
		LocalDateTime startDateTime = startOfMonth.atStartOfDay();
		LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

		// DB 조회 결과가 없음을 Optional.of(emptyList())로 설정
		when(healthRepository.findByMemberAndTypeAndCreatedAtBetween(
			eq(dummyMember),
			eq(Type.BLOOD_PRESSURE),
			eq(startDateTime),
			eq(endDateTime)
		)).thenReturn(Optional.of(Collections.emptyList()));

		// 월간 조회시, healthCommentService는 빈 리스트를 전달받으면 빈 문자열("")을 반환하도록 설정
		when(healthCommentService.getAiComment(
			eq(year),
			eq(month),
			eq(Type.BLOOD_PRESSURE),
			argThat(list -> list != null && list.isEmpty())
		)).thenReturn("");

		// when: 월간 건강 기록 조회 메서드 호출
		HealthRecordRes result = healthService.getMonthlyHealthRecords(verifyId, year, month, inputType);

		// then: 결과가 null이 아니며, recordGraphRes는 빈 리스트, healthComment는 빈 문자열이어야 함
		assertThat(result).isNotNull();
		assertThat(result.getRecordGraphRes()).isEmpty();
		assertThat(result.getHealthComment()).isEqualTo("");
	}
}