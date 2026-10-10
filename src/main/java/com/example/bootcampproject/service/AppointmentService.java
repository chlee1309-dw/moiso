package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.request.AppointmentCreateRequest;
import com.example.bootcampproject.dto.request.AppointmentStatusUpdateRequest;
import com.example.bootcampproject.dto.response.AppointmentResponse;
import com.example.bootcampproject.entity.AppointmentEntity;
import com.example.bootcampproject.entity.LocationEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.AppointmentStatus;
import com.example.bootcampproject.entity.enums.NotificationType;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.exception.InvalidStatusTransitionException;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.AppointmentRepository;
import com.example.bootcampproject.repository.LocationRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 멘토링 예약 신청 / 상태 변경 / 내 예약 목록. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final NotificationService notificationService;

    // 예약 신청, idx를 반환
    @Transactional
    public Long create(Long menteeIdx, AppointmentCreateRequest request) {
        UserEntity mentee = userRepository.findById(menteeIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", menteeIdx));
        UserEntity mentor = userRepository.findById(request.mentorIdx())
                .orElseThrow(() -> new ResourceNotFoundException("멘토", request.mentorIdx()));
        LocationEntity location = locationRepository.findById(request.locationIdx())
                .orElseThrow(() -> new ResourceNotFoundException("장소", request.locationIdx()));

        if (mentee.getIdx().equals(mentor.getIdx())) {
            throw new BusinessException("자기 자신에게 예약을 신청할 수 없습니다.");
        }
        if (!Boolean.TRUE.equals(location.getAvailable())) {
            throw new BusinessException("현재 예약할 수 없는 장소입니다.");
        }

        AppointmentEntity saved =
                appointmentRepository.save(request.toEntity(mentee, mentor, location));

        notificationService.notify(mentor.getIdx(), menteeIdx, NotificationType.APPOINTMENT_REQUESTED,
                mentee.getName() + "님이 멘토링을 예약했어요.", "/appointments");
        return saved.getIdx();
    }

    // 예약 상태 변경
    @Transactional
    public void changeStatus(Long appointmentIdx, Long loginUserIdx,
                             AppointmentStatusUpdateRequest request) {
        AppointmentEntity appointment = getAppointmentOrThrow(appointmentIdx);
        AppointmentStatus current = appointment.getStatus();
        AppointmentStatus next = request.status();

        if (!current.canTransitionTo(next)) {
            throw new InvalidStatusTransitionException(current, next);
        }

        boolean isMentor = appointment.getMentor().getIdx().equals(loginUserIdx);
        boolean isMentee = appointment.getMentee().getIdx().equals(loginUserIdx);
        if (!isMentor && !isMentee) {
            throw new ForbiddenException("이 예약의 당사자만 상태를 변경할 수 있습니다.");
        }

        switch (next) {
            case ACCEPTED, REJECTED, DONE -> {
                if (!isMentor) {
                    throw new ForbiddenException("수락 / 거절 / 완료는 멘토만 할 수 있습니다.");
                }
            }
            case CANCELLED -> { /* 멘토·멘티 모두 가능 */ }
            default -> throw new InvalidStatusTransitionException(current, next);
        }

        appointment.setStatus(next);

        Long recipientIdx = isMentor ? appointment.getMentee().getIdx() : appointment.getMentor().getIdx();
        notificationService.notify(recipientIdx, loginUserIdx, NotificationType.APPOINTMENT_STATUS_CHANGED,
                "예약이 '" + next.getLabel() + "' 상태로 변경되었습니다.", "/appointments");
    }

    // 내가 멘티로써 신청한 예약
    public List<AppointmentResponse> findAsMentee(Long userIdx) {
        return appointmentRepository.findByMenteeIdxWithDetails(userIdx)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    // 내가 멘토로써 받은 예약
    public List<AppointmentResponse> findAsMentor(Long userIdx) {
        return appointmentRepository.findByMentorIdxWithDetails(userIdx)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    private AppointmentEntity getAppointmentOrThrow(Long appointmentIdx) {
        return appointmentRepository.findById(appointmentIdx)
                .orElseThrow(() -> new ResourceNotFoundException("예약", appointmentIdx));
    }
}
