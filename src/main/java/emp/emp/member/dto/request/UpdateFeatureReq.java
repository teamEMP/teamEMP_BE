package emp.emp.member.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateFeatureReq {

	private String username;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthday;

	private String gender;

	private String address;
}
