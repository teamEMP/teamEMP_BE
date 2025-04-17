package emp.emp.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.auth.dto.LoginDto;
import emp.emp.auth.exception.AuthErrorCode;
import emp.emp.exception.BusinessException;
import emp.emp.member.dto.request.InputFeatureReq;
import emp.emp.member.dto.request.UpdateFeatureReq;
import emp.emp.member.dto.response.InputFeatureRes;
import emp.emp.member.entity.Member;
import emp.emp.member.enums.Role;
import emp.emp.member.repository.MemberRepository;
import emp.emp.util.jwt.JwtTokenProvider;
import emp.emp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;
	private final SecurityUtil securityUtil;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 유저 피처 입력
	 *
	 * @param userDetails 로그인된 유저
	 * @param request     유저 정보
	 * @return AT, RT
	 */
	@Override
	@Transactional
	public InputFeatureRes inputFeature(CustomUserDetails userDetails, InputFeatureReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		validRoleSemi(currentMember);

		inputUserInfo(currentMember, request);
		currentMember.setRole(Role.ROLE_USER);

		jwtTokenProvider.deleteRefreshToken(currentMember.getVerifyId());

		LoginDto updatedLoginDto = LoginDto.builder()
			.verifyId(currentMember.getVerifyId())
			.role(currentMember.getRole().name())
			.email(currentMember.getEmail())
			.build();

		CustomUserDetails updatedUserDetails = CustomUserDetails.create(updatedLoginDto);

		String accessToken = jwtTokenProvider.generateAccessToken(updatedUserDetails);
		String refreshToken = jwtTokenProvider.generateRefreshToken(updatedUserDetails);

		return InputFeatureRes.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	private void validRoleSemi(Member member) {
		if (!member.getRole().equals(Role.ROLE_SEMI_USER)) {
			throw new BusinessException(AuthErrorCode.INVALID_ROLE);
		}
	}

	private void inputUserInfo(Member member, InputFeatureReq request) {
		member.setUsername(request.getUsername());
		member.setGender(request.getGender());
		member.setBirthDay(request.getBirthday());
		member.setAddress(request.getAddress());
	}

	/**
	 * 유저 정보 업데이트
	 * @param request 새 유저 정보
	 */
	@Override
	@Transactional
	public void updateFeature(UpdateFeatureReq request) {
		Member currentMember = securityUtil.getCurrentMember();

		updateUserInfo(currentMember, request);
	}

	private void updateUserInfo(Member member, UpdateFeatureReq request) {
		member.setUsername(request.getUsername());
		member.setGender(request.getGender());
		member.setBirthDay(request.getBirthday());
		member.setAddress(request.getAddress());
	}
}
