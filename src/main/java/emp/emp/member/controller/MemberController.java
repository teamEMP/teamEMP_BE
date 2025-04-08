package emp.emp.member.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.member.dto.request.InputFeatureReq;
import emp.emp.member.dto.request.UpdateFeatureReq;
import emp.emp.member.dto.response.InputFeatureRes;
import emp.emp.member.entity.Member;
import emp.emp.member.service.MemberService;
import emp.emp.util.api_response.Response;
import emp.emp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

	private final MemberService memberService;
	private final SecurityUtil securityUtil;

	@GetMapping("/auth/semi/example")
	public ResponseEntity<Response<Map<String, Object>>> example(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		Map<String, Object> profile = new HashMap<>();
		profile.put("verifyId", userDetails.getName());
		profile.put("email", userDetails.getEmail());
		profile.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

		// 이외의 정보가 필요해서 Member 객체가 필요한 경우
		Member member = securityUtil.getCurrentMember();

		return Response.ok(profile).toResponseEntity();
	}

	/**
	 * 유저 피처 입력
	 *
	 * @param userDetails 로그인된 유저
	 * @param request     유저 정보
	 * @return AT, RT
	 */
	@PostMapping("/auth/semi/feature")
	public ResponseEntity<Response<InputFeatureRes>> inputFeature(
		@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody InputFeatureReq request) {
		InputFeatureRes response = memberService.inputFeature(userDetails, request);

		return Response.ok(response).toResponseEntity();
	}

	/**
	 * 유저 정보 업데이트
	 * @param request 새 유저 정보
	 */
	@PatchMapping("/auth/user/feature")
	public ResponseEntity<Response<Void>> updateFeature(@RequestBody UpdateFeatureReq request) {
		memberService.updateFeature(request);

		return Response.ok().toResponseEntity();
	}

}
