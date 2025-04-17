package emp.emp.health.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import emp.emp.health.entity.Health;
import emp.emp.health.enums.Type;
import emp.emp.member.entity.Member;

@Repository
public interface HealthRepository extends JpaRepository<Health, Long> {

	boolean existsByMemberAndTypeAndCreatedAtBetween(Member member, Type type, LocalDateTime start, LocalDateTime end);

	Optional<List<Health>> findByMemberAndTypeAndCreatedAtBetween(Member member, Type type, LocalDateTime start, LocalDateTime end);
}
