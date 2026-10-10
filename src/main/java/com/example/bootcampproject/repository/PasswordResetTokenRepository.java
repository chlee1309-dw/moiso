package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    // 새 토큰 발급 전, 그 사용자의 이전 미사용 토큰들을 무효화
    @Modifying
    @Query("update PasswordResetTokenEntity t set t.used = true " +
            "where t.user.idx = :userIdx and t.used = false")
    int invalidateActiveTokens(@Param("userIdx") Long userIdx);
}
