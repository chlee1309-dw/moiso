package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.response.FriendRelation;
import com.example.bootcampproject.dto.response.FriendSummaryResponse;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.repository.FriendRepository;
import com.example.bootcampproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FriendServiceTest {

    @Autowired
    FriendService friendService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRepository friendRepository;

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

    @Test
    void request_createsPendingRequest() {
        UserEntity a = createUser("FriendA");
        UserEntity b = createUser("FriendB");

        friendService.request(a.getIdx(), b.getIdx());

        assertEquals(FriendRelation.REQUESTED_BY_ME, friendService.getRelation(a.getIdx(), b.getIdx()));
        assertEquals(FriendRelation.REQUESTED_BY_THEM, friendService.getRelation(b.getIdx(), a.getIdx()));
        assertEquals(1, friendService.findIncomingRequests(b.getIdx()).size());
    }

    @Test
    void request_rejectsSelfRequest() {
        UserEntity a = createUser("FriendSelf");

        assertThrows(BusinessException.class, () -> friendService.request(a.getIdx(), a.getIdx()));
    }

    @Test
    void request_reciprocalRequestAutoAccepts() {
        UserEntity a = createUser("FriendRecipA");
        UserEntity b = createUser("FriendRecipB");

        friendService.request(a.getIdx(), b.getIdx());
        friendService.request(b.getIdx(), a.getIdx()); // 상대가 이미 보낸 요청에 맞요청 -> 자동 수락

        assertEquals(FriendRelation.FRIENDS, friendService.getRelation(a.getIdx(), b.getIdx()));
        assertEquals(0, friendService.findIncomingRequests(b.getIdx()).size());

        List<FriendSummaryResponse> aFriends = friendService.findFriends(a.getIdx());
        assertEquals(1, aFriends.size());
        assertEquals(b.getIdx(), aFriends.get(0).counterpartIdx());
    }

    @Test
    void request_duplicateRequestIsNoOp() {
        UserEntity a = createUser("FriendDupA");
        UserEntity b = createUser("FriendDupB");

        friendService.request(a.getIdx(), b.getIdx());
        friendService.request(a.getIdx(), b.getIdx());

        assertEquals(1, friendRepository.findIncomingRequests(b.getIdx()).size());
    }

    @Test
    void accept_onlyAddresseeCanAccept() {
        UserEntity a = createUser("FriendAcceptA");
        UserEntity b = createUser("FriendAcceptB");
        friendService.request(a.getIdx(), b.getIdx());
        Long friendIdx = friendRepository.findIncomingRequests(b.getIdx()).get(0).getIdx();

        assertThrows(ForbiddenException.class, () -> friendService.accept(friendIdx, a.getIdx()));

        friendService.accept(friendIdx, b.getIdx());
        assertEquals(FriendRelation.FRIENDS, friendService.getRelation(a.getIdx(), b.getIdx()));
    }
}
