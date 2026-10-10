package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.response.ChatMessageResponse;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ChatServiceTest {

    @Autowired
    ChatService chatService;

    @Autowired
    UserRepository userRepository;

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
    void getOrCreateRoom_reusesSameRoomRegardlessOfDirection() {
        UserEntity a = createUser("ChatA");
        UserEntity b = createUser("ChatB");

        Long roomFromA = chatService.getOrCreateRoom(a.getIdx(), b.getIdx());
        Long roomFromB = chatService.getOrCreateRoom(b.getIdx(), a.getIdx());

        assertEquals(roomFromA, roomFromB);
    }

    @Test
    void getOrCreateRoom_rejectsSelfChat() {
        UserEntity a = createUser("ChatSelf");

        assertThrows(BusinessException.class, () -> chatService.getOrCreateRoom(a.getIdx(), a.getIdx()));
    }

    @Test
    void sendMessage_persistsAndReturnsSenderInfo() {
        UserEntity a = createUser("ChatSenderA");
        UserEntity b = createUser("ChatSenderB");
        Long roomIdx = chatService.getOrCreateRoom(a.getIdx(), b.getIdx());

        ChatMessageResponse sent = chatService.sendMessage(roomIdx, a.getIdx(), "안녕하세요");

        assertNotNull(sent.idx());
        assertEquals(a.getIdx(), sent.senderIdx());
        assertEquals("안녕하세요", sent.content());
    }

    @Test
    void getMessagesAfter_returnsOnlyNewerMessages() {
        UserEntity a = createUser("ChatPollA");
        UserEntity b = createUser("ChatPollB");
        Long roomIdx = chatService.getOrCreateRoom(a.getIdx(), b.getIdx());

        ChatMessageResponse first = chatService.sendMessage(roomIdx, a.getIdx(), "첫 메시지");
        ChatMessageResponse second = chatService.sendMessage(roomIdx, b.getIdx(), "두번째 메시지");

        List<ChatMessageResponse> after = chatService.getMessagesAfter(roomIdx, a.getIdx(), first.idx());

        assertEquals(1, after.size());
        assertEquals(second.idx(), after.get(0).idx());
    }

    @Test
    void nonParticipant_cannotAccessRoom() {
        UserEntity a = createUser("ChatPrivA");
        UserEntity b = createUser("ChatPrivB");
        UserEntity intruder = createUser("ChatIntruder");
        Long roomIdx = chatService.getOrCreateRoom(a.getIdx(), b.getIdx());

        assertThrows(ForbiddenException.class, () -> chatService.getMessages(roomIdx, intruder.getIdx()));
        assertThrows(ForbiddenException.class, () -> chatService.sendMessage(roomIdx, intruder.getIdx(), "몰래 들어옴"));
    }
}
