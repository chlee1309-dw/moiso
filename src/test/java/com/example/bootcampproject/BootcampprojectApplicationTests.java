package com.example.bootcampproject;

import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;
import com.example.bootcampproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class BootcampprojectApplicationTests {

    @Autowired
    UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void transactionTest() {
        var user = UserEntity.builder()
                .name("Park Doe")
                .email("park-doe-" + System.nanoTime() + "@example.com")
                .password("test-password")
                .type("MENTEE")
                .purpose(Purpose.CAREER)
                .university("Example University")
                .department("Computer Science")
                .isVerified(false)
                .build();

        var entity = userRepository.save(user);
        entity.setName("Park Doe Updated");
    }
}
