package emp.emp.util.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.auth.exception.AuthErrorCode;
import emp.emp.exception.BusinessException;
import emp.emp.member.entity.Member;
import emp.emp.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

	private final MemberRepository memberRepository;

	public Member getCurrentMember() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
			String verifyId = userDetails.getName();

			return memberRepository.findByVerifyId(verifyId)
				.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));
		}

		throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
	}

}
