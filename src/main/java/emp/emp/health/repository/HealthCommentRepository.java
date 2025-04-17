package emp.emp.health.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import emp.emp.health.entity.HealthComment;

@Repository
public interface HealthCommentRepository extends JpaRepository<HealthComment, Long> {
}
