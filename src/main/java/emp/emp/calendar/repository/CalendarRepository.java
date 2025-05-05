package emp.emp.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import emp.emp.calendar.entity.CalendarEvent;
import emp.emp.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEvent, Long> {

  /**
   * 특정 멤버의 모든 일정을 찾는 메서드
   * @param member
   */
  List<CalendarEvent> findByMember(Member member);

  /**
   * 특정 기간 내의 일정을 찾는 메서드
   * @param start
   * @param end
   */
  List<CalendarEvent> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

  /**
   * 특정 이벤트 타입의 일정을 찾는 메서드
   * @param eventType
   */
  List<CalendarEvent> findByEventType(String eventType);

  /**
   * 특정 멤버와 기간으로 일정을 찾는 메서드
   * @param member
   * @param start
   * @param end
   */
  List<CalendarEvent> findByMemberAndStartDateBetween(Member member, LocalDateTime start, LocalDateTime end);

  /**
   * 특정 날짜의 일정을 찾는 메서드
   * @param start
   * @param end
   * @return
   */
  List<CalendarEvent> findByStartDateBetweenOrderByPriorityAsc(
          LocalDateTime start, LocalDateTime end);

  /**
   * 특정 멤버의 특정 날짜 일정을 우선순위대로 정렬하여 찾는 메서드
   * @param member
   * @param start
   * @param end
   * @return
   */
  List<CalendarEvent> findByMemberAndStartDateBetweenOrderByPriorityAsc(
          Member member, LocalDateTime start, LocalDateTime end);
}
