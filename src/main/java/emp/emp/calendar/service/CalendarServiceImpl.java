package emp.emp.calendar.service;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.calendar.dto.request.CalendarEventRequest;
import emp.emp.calendar.dto.response.CalendarEventResponse;
import emp.emp.calendar.entity.CalendarEvent;
import emp.emp.calendar.repository.CalendarRepository;
import emp.emp.member.entity.Member;
import emp.emp.member.repository.MemberRepository;
import emp.emp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

  private final CalendarRepository calendarRepository;
  private final MemberRepository memberRepository;
  private final SecurityUtil securityUtil;

  /**
   * [일정 등록]
   * 현재 로그인한 사용자의 정보로 CalendarEvent를 생성하여 저장
   */
  @Override
  @Transactional
  public CalendarEventResponse createEvent(CustomUserDetails userDetails, CalendarEventRequest request) {
    Member currentMember = securityUtil.getCurrentMember();

    CalendarEvent calendarEvent = CalendarEvent.builder()
            .member(currentMember)
            .eventType(request.getEventType())
            .title(request.getTitle())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

    calendarRepository.save(calendarEvent);

    return toResponse(calendarEvent);
  }

  /**
   * [일정 수정]
   * 이벤트 ID로 기존 일정을 조회하고
   * 본인 소유인지 검증 후 데이터를 수정
   */
  @Override
  @Transactional
  public CalendarEventResponse updateEvent(CustomUserDetails userDetails, Long eventId, CalendarEventRequest request) {
    Member currentMember = securityUtil.getCurrentMember();

    CalendarEvent calendarEvent = findByIdAndValidate(eventId, currentMember);

    calendarEvent.update(request);

    return toResponse(calendarEvent);
  }

  /**
   * [일정 삭제]
   * 이벤트 ID로 조회한 후
   * 본인 소유 일정이면 삭제
   */
  @Override
  @Transactional
  public void deleteEvent(CustomUserDetails userDetails, Long eventId) {
    Member currentMember = securityUtil.getCurrentMember();
    CalendarEvent calendarEvent = findByIdAndValidate(eventId, currentMember);

    calendarRepository.delete(calendarEvent);
  }

  /**
   * [특정 이벤트 조회]
   * 이벤트 ID로 조회한 후
   * 본인의 일정이 맞는지 확인하고 반환
   */
  @Override
  public CalendarEventResponse getEvent(CustomUserDetails userDetails, Long eventId) {
    Member currentMember = securityUtil.getCurrentMember();
    CalendarEvent calendarEvent = findByIdAndValidate(eventId, currentMember);

    return toResponse(calendarEvent);
  }

  /**
   * 로그인한 사용자의 전체 일정 조회
   */
  @Override
  public List<CalendarEventResponse> getAllEvents(CustomUserDetails userDetails) {
    Member currentMember = securityUtil.getCurrentMember();

    return calendarRepository.findByMember(currentMember).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * [일정 ID로 조회 + 현재 사용자의 일정인지 검증]
   * 본인이 소유한 일정이 아닐 경우 예외 발생
   */
  private CalendarEvent findByIdAndValidate(Long eventId, Member currentMember) {
    CalendarEvent calendarEvent = calendarRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

    if (!calendarEvent.getMember().equals(currentMember)) {
      throw new RuntimeException("접근 권한이 없습니다.");
    }

    return calendarEvent;
  }

  /**
   * CalendarEvent 엔티티를 CalendarEventResponse DTO로 변환
   */
  private CalendarEventResponse toResponse(CalendarEvent calendarEvent) {
    return CalendarEventResponse.builder()
            .eventId(calendarEvent.getEventId())
            .memberId(calendarEvent.getMember().getId())
            .eventType(calendarEvent.getEventType())
            .title(calendarEvent.getTitle())
            .startDate(calendarEvent.getStartDate())
            .endDate(calendarEvent.getEndDate())
            .build();
  }
}
