package emp.emp.member.entity;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import emp.emp.calendar.entity.CalendarEvent;
import emp.emp.family.entity.Family;
import emp.emp.member.enums.Role;
import emp.emp.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	private String provider;

	private String verifyId;

	private String username;

	private String email;

	private String password;

	@Enumerated(value = EnumType.STRING)
	private Role role;

	private String gender;

	private LocalDate birthDay;

	private String address;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "family_id")
	private Family family;

	@Builder
	public Member(String provider, String verifyId, String username, String email, String password, Role role,
		String gender, LocalDate birthDay, String address) {
		this.provider = provider;
		this.verifyId = verifyId;
		this.username = username;
		this.email = email;
		this.password = password;
		this.role = role;
		this.gender = gender;
		this.birthDay = birthDay;
		this.address = address;
	}

	public void setFamily(Family family) {
		this.family = family;
	}

	/**
	 * 생년월일 기준으로 만나이 계산
	 *
	 * @return 만나이
	 */
	public int getAge() {
		return Period.between(this.birthDay, LocalDate.now()).getYears();
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setBirthDay(LocalDate birthDay) {
		this.birthDay = birthDay;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<CalendarEvent> calendarEvent = new ArrayList<>();


}