package emp.emp.calendar.service;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.calendar.dto.request.CalendarEventRequest;
import emp.emp.calendar.dto.response.CalendarEventResponse;

import java.util.List;

public interface CalendarService {

  // 일정 등록
  CalendarEventResponse createEvent(CustomUserDetails userDetails, CalendarEventRequest request);

  // 일정 수정
  CalendarEventResponse updateEvent(CustomUserDetails userDetails, Long eventId, CalendarEventRequest request);

  // 일정 삭제
  void deleteEvent(CustomUserDetails userDetails, Long eventId);

  // 특정 이벤트 조회
  CalendarEventResponse getEvent(CustomUserDetails userDetails, Long eventId);

  // 회원의 전체 일정 조회
  List<CalendarEventResponse> getAllEvents(CustomUserDetails userDetails);
}
