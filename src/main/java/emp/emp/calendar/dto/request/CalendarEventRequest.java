package emp.emp.calendar.dto.request;

import emp.emp.calendar.enums.CalendarEventType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 캘린더 이벤트 등록, 수정용 DTO
 */
@Getter
@Setter
public class CalendarEventRequest {

  private Long verifyId;

  private CalendarEventType eventType;

  private String title;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

}
