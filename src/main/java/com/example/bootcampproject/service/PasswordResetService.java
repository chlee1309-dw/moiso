package com.example.bootcampproject.service;

import com.example.bootcampproject.entity.PasswordResetTokenEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.repository.PasswordResetTokenRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/** 비밀번호 재설정 메일 발송 + 토큰 검증. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final int TOKEN_VALID_MINUTES = 30;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JavaMailSender mailSender;

    @Value("${app.mail-from}")
    private String fromAddress;

    // 재설정 메일 발송. 가입 여부를 노출하지 않기 위해 이메일이 없어도 예외를 던지지 않고 조용히 무시
    @Transactional
    public void requestReset(String email, String resetLinkBase) {
        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.invalidateActiveTokens(user.getIdx());

            String token = UUID.randomUUID().toString().replace("-", "");
            PasswordResetTokenEntity entity =
                    new PasswordResetTokenEntity(token, LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES));
            entity.setUser(user);
            tokenRepository.save(entity);

            sendResetEmail(user, resetLinkBase + "?token=" + token);
        });
    }

    private void sendResetEmail(UserEntity user, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("[모이소] 비밀번호 재설정 안내");
        message.setText(
                user.getName() + "님, 안녕하세요.\n\n" +
                "비밀번호 재설정을 요청하셨습니다. 아래 링크를 눌러 새 비밀번호를 설정해 주세요.\n" +
                "(링크는 " + TOKEN_VALID_MINUTES + "분 동안만 유효합니다)\n\n" +
                resetLink + "\n\n" +
                "본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다."
        );
        try {
            mailSender.send(message);
        } catch (MailException e) {
            // 메일 서버 미설정/장애 시에도 토큰 자체는 이미 발급돼 있어 전체 요청을 실패시키지 않음
            log.error("비밀번호 재설정 메일 발송 실패: {}", user.getEmail(), e);
            throw new BusinessException("메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    // 토큰 검증 후 새 비밀번호 적용
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity entity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("유효하지 않은 링크입니다."));

        if (!entity.isValid()) {
            throw new BusinessException("만료되었거나 이미 사용된 링크입니다. 재설정을 다시 요청해 주세요.");
        }

        userService.resetPassword(entity.getUser().getIdx(), newPassword);
        entity.setUsed(true);
    }
}
