package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Transactional
    @Test
    void saveUser() {
        var user = UserEntity.builder()
                .name("Add-Doe")
                .email("add-doe-" + System.nanoTime() + "@example.com")
                .password("test-password")
                .type("MENTEE")
                .purpose(Purpose.CAREER)
                .university("Example University")
                .department("Computer Science")
                .isVerified(false)
                .build();

        var saved = userRepository.save(user);

        assertNotNull(saved.getIdx());
        assertNotNull(saved.getCreatedAt());
    }

    @Transactional
    @Test
    void deleteUser() {
        var user = userRepository.save(UserEntity.builder()
                .name("To-Delete")
                .email("to-delete-" + System.nanoTime() + "@example.com")
                .password("test-password")
                .type("MENTEE")
                .purpose(Purpose.CAREER)
                .university("Example University")
                .department("Computer Science")
                .isVerified(false)
                .build());

        userRepository.deleteById(user.getIdx());
    }
}
