package emp.emp.calendar.dto.response;

import emp.emp.calendar.enums.CalendarEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 캘린더 이벤트 조회용 DTO
 */
@Getter
@Builder
public class CalendarEventResponse {
  private Long eventId;
  private Long verifyId;
  private CalendarEventType eventType;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;

}
