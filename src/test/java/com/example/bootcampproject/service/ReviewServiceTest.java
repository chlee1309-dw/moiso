package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.request.ReviewCreateRequest;
import com.example.bootcampproject.entity.AppointmentEntity;
import com.example.bootcampproject.entity.LocationEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.AppointmentStatus;
import com.example.bootcampproject.entity.enums.Purpose;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.DuplicateReviewException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.repository.AppointmentRepository;
import com.example.bootcampproject.repository.LocationRepository;
import com.example.bootcampproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    ReviewService reviewService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    LocationRepository locationRepository;

    private UserEntity createUser(String name) {
        return userRepository.save(UserEntity.builder()
                .name(name)
                .email(name.toLowerCase() + "-" + System.nanoTime() + "@example.com")
                .password("test-password")
                .purpose(Purpose.CAREER)
                .university("Example University")
                .department("Computer Science")
                .isVerified(true)
                .build());
    }

    private AppointmentEntity createAppointment(UserEntity mentee, UserEntity mentor, AppointmentStatus status) {
        LocationEntity location = locationRepository.save(LocationEntity.builder()
                .name("리뷰테스트룸-" + System.nanoTime())
                .category("스터디룸")
                .available(true)
                .build());

        AppointmentEntity appointment = AppointmentEntity.builder()
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(10, 0))
                .status(status)
                .build();
        appointment.setMentee(mentee);
        appointment.setMentor(mentor);
        appointment.setLocation(location);
        return appointmentRepository.save(appointment);
    }

    @Test
    void create_succeedsForDoneAppointment() {
        UserEntity mentee = createUser("ReviewMentee1");
        UserEntity mentor = createUser("ReviewMentor1");
        AppointmentEntity appointment = createAppointment(mentee, mentor, AppointmentStatus.DONE);

        Long reviewIdx = reviewService.create(mentee.getIdx(),
                new ReviewCreateRequest(new BigDecimal("4.5"), "좋았어요", mentor.getIdx(), appointment.getIdx(), null));

        assertNotNull(reviewIdx);
        assertTrue(reviewService.hasReviewed(appointment.getIdx()));
    }

    @Test
    void create_rejectsNonDoneAppointment() {
        UserEntity mentee = createUser("ReviewMentee2");
        UserEntity mentor = createUser("ReviewMentor2");
        AppointmentEntity appointment = createAppointment(mentee, mentor, AppointmentStatus.ACCEPTED);

        assertThrows(BusinessException.class, () -> reviewService.create(mentee.getIdx(),
                new ReviewCreateRequest(new BigDecimal("5.0"), null, mentor.getIdx(), appointment.getIdx(), null)));
    }

    @Test
    void create_rejectsNonMenteeParticipant() {
        UserEntity mentee = createUser("ReviewMentee3");
        UserEntity mentor = createUser("ReviewMentor3");
        UserEntity stranger = createUser("ReviewStranger3");
        AppointmentEntity appointment = createAppointment(mentee, mentor, AppointmentStatus.DONE);

        assertThrows(ForbiddenException.class, () -> reviewService.create(stranger.getIdx(),
                new ReviewCreateRequest(new BigDecimal("5.0"), null, mentor.getIdx(), appointment.getIdx(), null)));
    }

    @Test
    void create_rejectsDuplicateReview() {
        UserEntity mentee = createUser("ReviewMentee4");
        UserEntity mentor = createUser("ReviewMentor4");
        AppointmentEntity appointment = createAppointment(mentee, mentor, AppointmentStatus.DONE);
        reviewService.create(mentee.getIdx(),
                new ReviewCreateRequest(new BigDecimal("4.0"), null, mentor.getIdx(), appointment.getIdx(), null));

        assertThrows(DuplicateReviewException.class, () -> reviewService.create(mentee.getIdx(),
                new ReviewCreateRequest(new BigDecimal("3.0"), null, mentor.getIdx(), appointment.getIdx(), null)));
    }

    @Test
    void getMentorAverageRating_computesAverage() {
        UserEntity mentor = createUser("ReviewMentor5");
        UserEntity mentee1 = createUser("ReviewMentee5a");
        UserEntity mentee2 = createUser("ReviewMentee5b");
        AppointmentEntity appt1 = createAppointment(mentee1, mentor, AppointmentStatus.DONE);
        AppointmentEntity appt2 = createAppointment(mentee2, mentor, AppointmentStatus.DONE);

        reviewService.create(mentee1.getIdx(),
                new ReviewCreateRequest(new BigDecimal("4.0"), null, mentor.getIdx(), appt1.getIdx(), null));
        reviewService.create(mentee2.getIdx(),
                new ReviewCreateRequest(new BigDecimal("5.0"), null, mentor.getIdx(), appt2.getIdx(), null));

        assertEquals(4.5, reviewService.getMentorAverageRating(mentor.getIdx()), 0.001);
    }
}
