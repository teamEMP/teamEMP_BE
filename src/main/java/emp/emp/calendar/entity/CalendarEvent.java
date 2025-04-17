package emp.emp.calendar.entity;

import emp.emp.calendar.dto.request.CalendarEventRequest;
import emp.emp.calendar.enums.CalendarEventType;
import emp.emp.member.entity.Member;
import emp.emp.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "event_id")
  private Long eventId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Enumerated(EnumType.STRING)
  private CalendarEventType eventType;

  private String title;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

  public void update(CalendarEventRequest request) {
    this.eventType = request.getEventType(); // enum 타입으로 매핑됨
    this.title = request.getTitle();
    this.startDate = request.getStartDate();
    this.endDate = request.getEndDate();
  }
}
