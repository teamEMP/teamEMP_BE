package emp.emp.auth.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.auth.exception.AuthErrorCode;
import emp.emp.auth.own.dto.LoginRequest;
import emp.emp.auth.own.dto.RegisterRequest;
import emp.emp.auth.own.service.AuthService;
import emp.emp.exception.BusinessException;
import emp.emp.util.api_response.Response;
import emp.emp.util.api_response.error_code.GeneralErrorCode;
import emp.emp.util.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

	// @Value("${redirect-url.frontend.user}")
	// private String REDIRECT_URL_USER;
	//
	// @Value("${redirect-url.frontend.semi-user}")
	// private String REDIRECT_URL_SEMI_USER;

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	@Value("${redirect-url.frontend.main}")
	private String REDIRECT_URL_MAIN;

	@GetMapping("/login-failed")
	public ResponseEntity<Response<Object>> loginFailedExample() {
		return Response.errorResponse(AuthErrorCode.EMAIL_DUPLICATED).toResponseEntity();
	}

	@GetMapping("/login-success")
	public ResponseEntity<Response<Void>> loginSuccessExample(HttpServletRequest request) {
		System.out.println(request.getRequestURI());
		return Response.ok().toResponseEntity();
	}

	/**
	 * 자체 로그인
	 *
	 * @param request  로그인에 필요한 유저 정보
	 * @param response redirect를 위한 HttpServletResponse
	 * @throws IOException redirect 예외
	 * 성공 시 임시 코드 발급 및 리다이렉트
	 */
	@PostMapping("/login")
	public void login(@RequestBody LoginRequest request, HttpServletResponse response) throws IOException {
		CustomUserDetails userDetails = (CustomUserDetails)authService.loadUserByUsername(
			request.getEmail());

		if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
			throw new BusinessException(AuthErrorCode.INVALID_LOGIN_ARGUMENT);
		}

		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);

		String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

		String tempCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

		Map<String, String> tokenData = Map.of(
			"accessToken", accessToken,
			"refreshToken", refreshToken
		);

		redisTemplate.opsForValue().set(tempCode, objectMapper.writeValueAsString(tokenData),
			5, TimeUnit.MINUTES);

		response.setStatus(HttpStatus.OK.value());
		response.sendRedirect(REDIRECT_URL_MAIN + "?code=" + tempCode);

		/*String role = userDetails.getAuthorities().iterator().next().getAuthority();

		if ("ROLE_USER".equals(role)) {
			response.sendRedirect(REDIRECT_URL_USER);
		} else if ("ROLE_SEMI_USER".equals(role)) {
			response.sendRedirect(REDIRECT_URL_SEMI_USER);
		}*/
	}

	/**
	 * 자체 회원가입
	 *
	 * @param request 회원가입을 위한 정보
	 * @return 200 ok
	 */
	@PostMapping("/register")
	public ResponseEntity<Response<Void>> register(@RequestBody @Valid RegisterRequest request) {
		authService.register(request);

		return Response.ok().toResponseEntity();
	}

	/**
	 * Refresh-Token을 사용해 새 토큰들을 발급받는 메서드
	 *
	 * @return AT, RT
	 */
	@GetMapping("/token/refresh")
	public ResponseEntity<Response<Map<String, String>>> tokenRefresh(HttpServletRequest request) {

		String refreshToken = request.getHeader("Refresh-Token");
		if (refreshToken == null) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		Map<String, String> tokens = jwtTokenProvider.refreshTokens(refreshToken);

		return Response.ok(tokens).toResponseEntity();
	}

	/**
	 * 임시 코드를 이용해 AT, RT를 주는 메서드
	 *
	 * @param tempCode 임시 코드
	 * @return 200 AT, RT
	 * @throws IOException
	 */
	@PostMapping("/token/exchange")
	public ResponseEntity<Response<Map<String, String>>> exchangeToken(@RequestParam("code") String tempCode) throws
		IOException {

		String tokenDataJson = redisTemplate.opsForValue().get(tempCode);

		if (tokenDataJson == null) {
			throw new BusinessException(GeneralErrorCode.BAD_REQUEST);
		}

		redisTemplate.delete(tempCode);

		Map<String, String> tokenData = objectMapper.readValue(
			tokenDataJson, new TypeReference<Map<String, String>>() {
			});

		String accessToken = tokenData.get("accessToken");
		if (accessToken != null) {
			Claims claims = jwtTokenProvider.getClaims(accessToken);
			String role = (String) claims.get("role");
			tokenData.put("role", role);
		}

		return Response.ok(tokenData).toResponseEntity();
	}

}
