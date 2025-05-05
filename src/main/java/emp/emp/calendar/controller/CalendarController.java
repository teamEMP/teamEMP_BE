package emp.emp.calendar.controller;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.calendar.dto.request.CalendarEventRequest;
import emp.emp.calendar.dto.response.CalendarEventResponse;
import emp.emp.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auth/user/calendar")
@RequiredArgsConstructor
public class CalendarController {

  private final CalendarService calendarService;

  /**
   * 캘린더 일정 등록
   */
  @PostMapping
  public CalendarEventResponse createEvent(
          @AuthenticationPrincipal CustomUserDetails userDetails,
          @RequestBody CalendarEventRequest request
  ) {
    return calendarService.createEvent(userDetails, request);
  }

  /**
   * 캘린더 일정 수정
   */
  @PutMapping("/{eventId}")
  public CalendarEventResponse updateEvent(
          @AuthenticationPrincipal CustomUserDetails userDetails,
          @PathVariable Long eventId,
          @RequestBody CalendarEventRequest request
  ) {
    return calendarService.updateEvent(userDetails, eventId, request);
  }

  /**
   * 캘린더 일정 삭제
   */
  @DeleteMapping("/{eventId}")
  public void deleteEvent(
          @AuthenticationPrincipal CustomUserDetails userDetails,
          @PathVariable Long eventId
  ) {
    calendarService.deleteEvent(userDetails, eventId);
  }

  /**
   * 캘린더 일정 하나하나 조회
   */
  @GetMapping("/{eventId}")
  public CalendarEventResponse getEvent(
          @AuthenticationPrincipal CustomUserDetails userDetails,
          @PathVariable Long eventId
  ) {
    return calendarService.getEvent(userDetails, eventId);
  }

  /**
   * 캘린더 일정 전체 조회
   */
  @GetMapping
  public List<CalendarEventResponse> getAllEvents(
          @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return calendarService.getAllEvents(userDetails);
  }

  /**
   * 특정 날짜 일정 조회
   */
  @GetMapping("/date")
  public List<CalendarEventResponse> getEventsByDate(
          @AuthenticationPrincipal CustomUserDetails userDetails,
          @RequestParam LocalDateTime date
  ) {
    return calendarService.getEventsByDate(userDetails, date);
  }

  /**
   * 캘린더 일정 우선순위 업데이트
   */
  @PutMapping("/{eventId}/priority")
  public CalendarEventResponse updatePriority(
          @AuthenticationPrincipal CustomUserDetails userDetails,
          @PathVariable Long eventId,
          @RequestBody Integer priority
  ) {
    return calendarService.updatePriority(userDetails, eventId, priority);
  }


}
