package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.response.NotificationResponse;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.NotificationType;
import com.example.bootcampproject.entity.enums.Purpose;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    NotificationService notificationService;

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
    void notify_skipsSelfNotification() {
        UserEntity user = createUser("NotifSelf");

        notificationService.notify(user.getIdx(), user.getIdx(), NotificationType.LIKE_QUESTION, "내용", "/questions/1");

        assertEquals(0, notificationService.countUnread(user.getIdx()));
    }

    @Test
    void notify_createsNotificationForOthers() {
        UserEntity recipient = createUser("NotifRecipient");
        UserEntity actor = createUser("NotifActor");

        notificationService.notify(recipient.getIdx(), actor.getIdx(), NotificationType.ANSWER,
                "답변이 달렸습니다.", "/questions/1");

        List<NotificationResponse> recent = notificationService.findRecent(recipient.getIdx());
        assertEquals(1, recent.size());
        assertEquals(NotificationType.ANSWER, recent.get(0).type());
        assertFalse(recent.get(0).read());
        assertEquals(1, notificationService.countUnread(recipient.getIdx()));
    }

    @Test
    void markReadAndGetLink_onlyWorksForOwner() {
        UserEntity recipient = createUser("NotifOwner");
        UserEntity stranger = createUser("NotifStranger");
        UserEntity actor = createUser("NotifOwnerActor");
        notificationService.notify(recipient.getIdx(), actor.getIdx(), NotificationType.CHAT_MESSAGE, "메시지", "/chats/1");
        Long notifIdx = notificationService.findRecent(recipient.getIdx()).get(0).idx();

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markReadAndGetLink(notifIdx, stranger.getIdx()));

        String link = notificationService.markReadAndGetLink(notifIdx, recipient.getIdx());
        assertEquals("/chats/1", link);
        assertEquals(0, notificationService.countUnread(recipient.getIdx()));
    }

    @Test
    void markAllRead_marksEverythingRead() {
        UserEntity recipient = createUser("NotifBulk");
        UserEntity actor = createUser("NotifBulkActor");
        notificationService.notify(recipient.getIdx(), actor.getIdx(), NotificationType.LIKE_QUESTION, "1", "/questions/1");
        notificationService.notify(recipient.getIdx(), actor.getIdx(), NotificationType.LIKE_ANSWER, "2", "/questions/2");
        assertEquals(2, notificationService.countUnread(recipient.getIdx()));

        notificationService.markAllRead(recipient.getIdx());

        assertEquals(0, notificationService.countUnread(recipient.getIdx()));
    }
}
