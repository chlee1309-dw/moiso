package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.response.FriendRelation;
import com.example.bootcampproject.dto.response.FriendRequestResponse;
import com.example.bootcampproject.dto.response.FriendSummaryResponse;
import com.example.bootcampproject.entity.FriendEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.FriendStatus;
import com.example.bootcampproject.entity.enums.NotificationType;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.FriendRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** 친구 요청 / 수락 / 거절 / 목록. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // 친구 요청. 상대가 이미 나에게 요청해 둔 상태라면 바로 수락 처리(자동 매칭)
    @Transactional
    public void request(Long actorIdx, Long targetIdx) {
        if (actorIdx.equals(targetIdx)) {
            throw new BusinessException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
        UserEntity actor = userRepository.findById(actorIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", actorIdx));
        UserEntity target = userRepository.findById(targetIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", targetIdx));

        Optional<FriendEntity> existing = friendRepository.findRelation(actorIdx, targetIdx);
        if (existing.isPresent()) {
            FriendEntity friend = existing.get();
            if (friend.getStatus() == FriendStatus.ACCEPTED) {
                return; // 이미 친구
            }
            if (friend.getRequester().getIdx().equals(targetIdx)) {
                // 상대가 먼저 나에게 요청해 둔 상태 -> 자동 수락
                friend.setStatus(FriendStatus.ACCEPTED);
                notificationService.notify(targetIdx, actorIdx, NotificationType.FRIEND_ACCEPTED,
                        actor.getName() + "님과 친구가 되었습니다.", "/users/" + actorIdx);
            }
            return; // 내가 이미 요청해 둔 상태 -> 중복 요청 무시
        }

        FriendEntity friend = new FriendEntity();
        friend.setRequester(actor);
        friend.setAddressee(target);
        friend.setStatus(FriendStatus.REQUESTED);
        friendRepository.save(friend);

        notificationService.notify(targetIdx, actorIdx, NotificationType.FRIEND_REQUEST,
                actor.getName() + "님이 친구 요청을 보냈어요.", "/friends");
    }

    // 요청 수락 (받은 사람만 가능)
    @Transactional
    public void accept(Long friendIdx, Long userIdx) {
        FriendEntity friend = getOrThrow(friendIdx);
        if (!friend.getAddressee().getIdx().equals(userIdx)) {
            throw new ForbiddenException("본인에게 온 요청만 수락할 수 있습니다.");
        }
        friend.setStatus(FriendStatus.ACCEPTED);
        notificationService.notify(friend.getRequester().getIdx(), userIdx, NotificationType.FRIEND_ACCEPTED,
                friend.getAddressee().getName() + "님이 친구 요청을 수락했어요.", "/users/" + userIdx);
    }

    // 요청 거절 (받은 사람만 가능) - 그냥 삭제
    @Transactional
    public void reject(Long friendIdx, Long userIdx) {
        FriendEntity friend = getOrThrow(friendIdx);
        if (!friend.getAddressee().getIdx().equals(userIdx)) {
            throw new ForbiddenException("본인에게 온 요청만 거절할 수 있습니다.");
        }
        friendRepository.delete(friend);
    }

    // 친구 삭제(끊기) - 당사자만 가능
    @Transactional
    public void remove(Long friendIdx, Long userIdx) {
        FriendEntity friend = getOrThrow(friendIdx);
        boolean isParticipant = friend.getRequester().getIdx().equals(userIdx)
                || friend.getAddressee().getIdx().equals(userIdx);
        if (!isParticipant) {
            throw new ForbiddenException("본인의 친구 관계만 삭제할 수 있습니다.");
        }
        friendRepository.delete(friend);
    }

    public List<FriendSummaryResponse> findFriends(Long userIdx) {
        return friendRepository.findAcceptedByUserIdx(userIdx).stream()
                .map(f -> {
                    UserEntity counterpart = f.getRequester().getIdx().equals(userIdx) ? f.getAddressee() : f.getRequester();
                    return new FriendSummaryResponse(f.getIdx(), counterpart.getIdx(), counterpart.getName(),
                            counterpart.getUniversity(), counterpart.getDepartment());
                })
                .toList();
    }

    public List<FriendRequestResponse> findIncomingRequests(Long userIdx) {
        return friendRepository.findIncomingRequests(userIdx).stream()
                .map(f -> new FriendRequestResponse(f.getIdx(), f.getRequester().getIdx(),
                        f.getRequester().getName(), f.getCreatedAt()))
                .toList();
    }

    // 프로필 화면의 "친구 추가" 버튼 상태 결정용
    public FriendRelation getRelation(Long userIdx, Long otherIdx) {
        if (userIdx.equals(otherIdx)) {
            return FriendRelation.NONE;
        }
        return friendRepository.findRelation(userIdx, otherIdx)
                .map(f -> {
                    if (f.getStatus() == FriendStatus.ACCEPTED) {
                        return FriendRelation.FRIENDS;
                    }
                    return f.getRequester().getIdx().equals(userIdx)
                            ? FriendRelation.REQUESTED_BY_ME
                            : FriendRelation.REQUESTED_BY_THEM;
                })
                .orElse(FriendRelation.NONE);
    }

    private FriendEntity getOrThrow(Long friendIdx) {
        return friendRepository.findById(friendIdx)
                .orElseThrow(() -> new ResourceNotFoundException("친구 요청", friendIdx));
    }
}
