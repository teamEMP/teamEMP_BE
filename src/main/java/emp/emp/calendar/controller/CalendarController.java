package emp.emp.calendar.controller;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.calendar.dto.request.CalendarEventRequest;
import emp.emp.calendar.dto.response.CalendarEventResponse;
import emp.emp.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/user/api/calendar")
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
}
