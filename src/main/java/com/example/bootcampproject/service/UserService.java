package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.request.LoginRequest;
import com.example.bootcampproject.dto.request.PasswordChangeRequest;
import com.example.bootcampproject.dto.request.ProfileUpdateRequest;
import com.example.bootcampproject.dto.request.SignUpRequest;
import com.example.bootcampproject.dto.response.UserPublicProfileResponse;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.exception.DuplicateEmailException;
import com.example.bootcampproject.exception.InvalidPasswordException;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.AnswerRepository;
import com.example.bootcampproject.repository.QuestionRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** 회원가입 / 로그인 / 프로필 조회·수정 / 비밀번호 변경. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public UserResponse signup(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        UserEntity saved = userRepository.save(request.toEntity(encodedPassword));
        return UserResponse.from(saved);
    }

    public UserResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidPasswordException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return UserResponse.from(user);
    }

    // 프로필 조회
    public UserResponse getProfile(Long userIdx) {
        return UserResponse.from(getUserOrThrow(userIdx));
    }

    // 프로필 수정
    @Transactional
    public UserResponse updateProfile(Long userIdx, ProfileUpdateRequest request) {
        UserEntity user = getUserOrThrow(userIdx);
        request.applyTo(user);
        return UserResponse.from(user);
    }

    // 비밀번호 수정
    @Transactional
    public void changePassword(Long userIdx, PasswordChangeRequest request) {
        UserEntity user = getUserOrThrow(userIdx);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    // 비밀번호 재설정 (현재 비밀번호 확인 없이 강제 변경 - 이메일 인증된 재설정 플로우 전용)
    @Transactional
    public void resetPassword(Long userIdx, String newRawPassword) {
        UserEntity user = getUserOrThrow(userIdx);
        user.setPassword(passwordEncoder.encode(newRawPassword));
    }

    // 이름+학교+학과로 가입된 이메일(로그인 아이디) 찾기 - 마스킹해서 반환
    public String findMaskedEmail(String name, String university, String department) {
        var matches = userRepository.findByNameAndUniversityAndDepartment(name, university, department);
        if (matches.size() != 1) {
            throw new ResourceNotFoundException("입력하신 정보와 일치하는 회원을 찾을 수 없습니다.");
        }
        return maskEmail(matches.get(0).getEmail());
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) {
            return email; // 로컬파트가 너무 짧으면 마스킹 없이 그대로
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        return local.substring(0, 2) + "*".repeat(local.length() - 2) + domain;
    }

    // 다른 사용자에게 보여주는 공개 프로필 (이름/학교/작성글·답변 수)
    public UserPublicProfileResponse getPublicProfile(Long userIdx) {
        UserEntity user = getUserOrThrow(userIdx);
        long questionCount = questionRepository.countByWriter_Idx(userIdx);
        long answerCount = answerRepository.countByWriter_Idx(userIdx);
        return UserPublicProfileResponse.of(user, questionCount, answerCount);
    }

    // 캘린더 구독(webcal) 토큰 - 없으면 새로 발급해서 반환
    @Transactional
    public String getOrCreateCalendarToken(Long userIdx) {
        UserEntity user = getUserOrThrow(userIdx);
        if (user.getCalendarToken() == null) {
            user.setCalendarToken(UUID.randomUUID().toString().replace("-", ""));
        }
        return user.getCalendarToken();
    }

    // 캘린더 구독 토큰으로 사용자 idx 조회
    public Long getUserIdxByCalendarToken(String token) {
        return userRepository.findByCalendarToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("캘린더 구독 링크", token))
                .getIdx();
    }

    private UserEntity getUserOrThrow(Long userIdx) {
        return userRepository.findById(userIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", userIdx));
    }
}
