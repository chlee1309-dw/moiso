package com.example.bootcampproject.service;

import com.example.bootcampproject.entity.PasswordResetTokenEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.repository.PasswordResetTokenRepository;
import com.example.bootcampproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class PasswordResetServiceTest {

    @Autowired
    PasswordResetService passwordResetService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository tokenRepository;

    @MockitoBean
    JavaMailSender mailSender;

    private UserEntity createUser(String name) {
        return userRepository.save(UserEntity.builder()
                .name(name)
                .email(name.toLowerCase() + "-" + System.nanoTime() + "@example.com")
                .password("encoded-password")
                .purpose(Purpose.CAREER)
                .university("Example University")
                .department("Computer Science")
                .isVerified(true)
                .build());
    }

    @Test
    void requestReset_sendsMailOnlyWhenEmailMatches() {
        UserEntity user = createUser("ResetMailUser");

        passwordResetService.requestReset(user.getEmail(), "http://localhost/reset-password");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        assertEquals(user.getEmail(), captor.getValue().getTo()[0]);

        // 가입되지 않은 이메일 -> 조용히 무시 (추가 발송 없음)
        passwordResetService.requestReset("no-such-user-" + System.nanoTime() + "@example.com", "http://localhost/reset-password");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void resetPassword_rejectsInvalidToken() {
        assertThrows(BusinessException.class,
                () -> passwordResetService.resetPassword("no-such-token", "newpassword1"));
    }

    @Test
    void resetPassword_rejectsExpiredToken() {
        UserEntity user = createUser("ResetExpiredUser");
        PasswordResetTokenEntity expired =
                new PasswordResetTokenEntity("expired-token-" + System.nanoTime(), LocalDateTime.now().minusMinutes(1));
        expired.setUser(user);
        tokenRepository.save(expired);

        assertThrows(BusinessException.class,
                () -> passwordResetService.resetPassword(expired.getToken(), "newpassword1"));
    }

    @Test
    void resetPassword_validToken_updatesPasswordAndMarksUsed() {
        UserEntity user = createUser("ResetValidUser");
        String rawToken = "valid-token-" + System.nanoTime();
        PasswordResetTokenEntity token = new PasswordResetTokenEntity(rawToken, LocalDateTime.now().plusMinutes(30));
        token.setUser(user);
        tokenRepository.save(token);

        passwordResetService.resetPassword(rawToken, "brandNewPassword1");

        assertTrue(tokenRepository.findByToken(rawToken).orElseThrow().isUsed());
        // 같은 토큰 재사용 시도는 막힘
        assertThrows(BusinessException.class,
                () -> passwordResetService.resetPassword(rawToken, "anotherPassword2"));
    }
}
