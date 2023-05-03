package com.member.service;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.member.entity.MemberEntity;
import com.member.exception.CustomException;
import com.member.model.LoginRequest;
import com.member.model.LoginResponse;
import com.member.model.Member;
import com.member.model.MyInfoResponse;
import com.member.model.SignRequest;
import com.member.model.SignResponse;
import com.member.repository.MemberRepository;
import com.member.utils.CryptoUtil;
import com.member.utils.JwtTokenUtil;
import com.member.utils.MessageUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	public SignResponse createMember(SignRequest req) throws Exception {

		if(ObjectUtils.isEmpty(req.getUserId())){
			throw new CustomException(MessageUtils.INVALID_USER_ID);
		}else if(ObjectUtils.isEmpty(req.getName())) {
			throw new CustomException(MessageUtils.INVALID_NAME);
		}else if(ObjectUtils.isEmpty(req.getPassword())) {
			throw new CustomException(MessageUtils.INVALID_PASSWORD);
		}else if(ObjectUtils.isEmpty(req.getRegNo())) {
			throw new CustomException(MessageUtils.INVALID_REG_NO);
		}
		
		SignResponse rst = new SignResponse();
		
		String encRegNo = CryptoUtil.encryptAES256(req.getRegNo());
		
		// 회원가입이 되어있는 것 체크
		MemberEntity rstMember = memberRepository.findMemberByNmReg(req.getName(), encRegNo);

		if (!ObjectUtils.isEmpty(rstMember)) {
			throw new CustomException(MessageUtils.DUPLICATE_USER);
		}

		MemberEntity saveMember = memberRepository.save(
				MemberEntity.builder()
				.userId(req.getUserId())
				.password(CryptoUtil.encryptAES256(req.getPassword()))
				.name(req.getName()).regNo(encRegNo)
				.build());
		
		rst.setName(saveMember.getName());
		rst.setUserId(saveMember.getUserId());
		
		return rst;

	}
	
	public LoginResponse login(LoginRequest req) throws Exception {

		if(ObjectUtils.isEmpty(req.getUserId())){
			throw new CustomException(MessageUtils.INVALID_USER_ID);
		}else if(ObjectUtils.isEmpty(req.getPassword())) {
			throw new CustomException(MessageUtils.INVALID_PASSWORD);
		}
		
		String encPwd = CryptoUtil.encryptAES256(req.getPassword());
		MemberEntity rstMember =  memberRepository.findMemberByIdPw(req.getUserId(), encPwd)
				.orElseThrow(() -> new CustomException(MessageUtils.INVALID_USER));

		// 토큰 발급 및 로그인 처리
		LoginResponse res = new LoginResponse();
		res.setToken(JwtTokenUtil.createToken(rstMember.getUserId(), rstMember.getName()));
		return res;
	}
	
	public MyInfoResponse myInfo(String token) throws Exception {
		
		if(!ObjectUtils.isEmpty(token)) {
			Member member = JwtTokenUtil.autholriztionCheckUser(token); // 넘겨받은 토큰 값으로 토큰에 있는 값 꺼내기
			
			log.info("szs/me : {}", member);
			
			MemberEntity memberEntity =  memberRepository.findById(member.getUserId())
					.orElseThrow(() -> new CustomException(MessageUtils.INVALID_USER)); //토큰 claims에 담겨 있는 userId로 회원 정보 조회
			
			//TODO 비밀번호, 주민번호 암호화 해서 가야하는지 확인 필요
			MyInfoResponse result = new MyInfoResponse();
			result.setName(memberEntity.getName());
			result.setPassword(CryptoUtil.decryptAES256(memberEntity.getPassword()));
			result.setRegNo(CryptoUtil.decryptAES256(memberEntity.getRegNo()));
			result.setUserId(memberEntity.getUserId());
			
			return result;
		}else {
			throw new CustomException(MessageUtils.INVALID_TOKEN);
		}
	}
	
}
