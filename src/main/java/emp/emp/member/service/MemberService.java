package emp.emp.member.service;

import emp.emp.auth.custom.CustomUserDetails;
import emp.emp.member.dto.request.InputFeatureReq;
import emp.emp.member.dto.request.UpdateFeatureReq;
import emp.emp.member.dto.response.InputFeatureRes;

public interface MemberService {

	/**
	 * 유저 피처 입력
	 *
	 * @param userDetails 로그인된 유저
	 * @param request     유저 정보
	 * @return AT, RT
	 */
	InputFeatureRes inputFeature(CustomUserDetails userDetails, InputFeatureReq request);

	/**
	 * 유저 정보 업데이트
	 * @param request 새 유저 정보
	 */
	void updateFeature(UpdateFeatureReq request);
}
