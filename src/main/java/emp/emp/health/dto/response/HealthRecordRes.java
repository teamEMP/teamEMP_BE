package emp.emp.health.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordRes {

	List<HealthRecordGraphRes> recordGraphRes;
	private String healthComment;

}
