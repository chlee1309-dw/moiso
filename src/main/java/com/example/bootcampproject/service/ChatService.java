package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.response.ChatMessageResponse;
import com.example.bootcampproject.dto.response.ChatRoomSummaryResponse;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.entity.ChatMessageEntity;
import com.example.bootcampproject.entity.ChatRoomEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.NotificationType;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.ChatMessageRepository;
import com.example.bootcampproject.repository.ChatRoomRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** 1:1 채팅방 개설 / 메시지 송수신 (폴링 방식). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // 두 사용자의 채팅방을 찾거나 새로 만들어 idx 반환
    @Transactional
    public Long getOrCreateRoom(Long userIdx, Long counterpartIdx) {
        if (userIdx.equals(counterpartIdx)) {
            throw new BusinessException("자기 자신과는 채팅할 수 없습니다.");
        }
        Long lowerIdx = Math.min(userIdx, counterpartIdx);
        Long higherIdx = Math.max(userIdx, counterpartIdx);

        return chatRoomRepository.findByUserA_IdxAndUserB_Idx(lowerIdx, higherIdx)
                .map(ChatRoomEntity::getIdx)
                .orElseGet(() -> createRoom(lowerIdx, higherIdx));
    }

    private Long createRoom(Long lowerIdx, Long higherIdx) {
        UserEntity userA = userRepository.findById(lowerIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", lowerIdx));
        UserEntity userB = userRepository.findById(higherIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", higherIdx));

        ChatRoomEntity room = new ChatRoomEntity();
        room.setUserA(userA);
        room.setUserB(userB);
        return chatRoomRepository.save(room).getIdx();
    }

    // 내가 참여 중인 채팅방 목록 (마지막 메시지 미리보기 포함), 최근 메시지순
    public List<ChatRoomSummaryResponse> findMyRooms(Long userIdx) {
        return chatRoomRepository.findMyRooms(userIdx).stream()
                .map(room -> {
                    UserEntity counterpart = room.getUserA().getIdx().equals(userIdx)
                            ? room.getUserB() : room.getUserA();
                    Optional<ChatMessageEntity> last =
                            chatMessageRepository.findTopByRoom_IdxOrderByIdxDesc(room.getIdx());

                    return new ChatRoomSummaryResponse(
                            room.getIdx(),
                            counterpart.getIdx(),
                            counterpart.getName(),
                            last.map(ChatMessageEntity::getContent).orElse("아직 메시지가 없어요."),
                            last.map(ChatMessageEntity::getCreatedAt).orElse(room.getCreatedAt())
                    );
                })
                .sorted((a, b) -> b.lastMessageAt().compareTo(a.lastMessageAt()))
                .toList();
    }

    // 상대방 정보 (채팅방 헤더용) - 참여자가 아니면 접근 거부
    public UserResponse getCounterpart(Long roomIdx, Long userIdx) {
        ChatRoomEntity room = checkParticipant(roomIdx, userIdx);
        UserEntity counterpart = room.getUserA().getIdx().equals(userIdx) ? room.getUserB() : room.getUserA();
        return UserResponse.from(counterpart);
    }

    public List<ChatMessageResponse> getMessages(Long roomIdx, Long userIdx) {
        checkParticipant(roomIdx, userIdx);
        return chatMessageRepository.findByRoomIdxOrderByIdxAsc(roomIdx).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    public List<ChatMessageResponse> getMessagesAfter(Long roomIdx, Long userIdx, Long afterIdx) {
        checkParticipant(roomIdx, userIdx);
        return chatMessageRepository.findByRoomIdxAfter(roomIdx, afterIdx).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long roomIdx, Long senderIdx, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("메시지 내용을 입력해 주세요.");
        }
        ChatRoomEntity room = checkParticipant(roomIdx, senderIdx);
        UserEntity sender = userRepository.findById(senderIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", senderIdx));

        ChatMessageEntity message = ChatMessageEntity.builder()
                .content(content.strip())
                .build();
        message.setRoom(room);
        message.setSender(sender);
        ChatMessageResponse saved = ChatMessageResponse.from(chatMessageRepository.save(message));

        Long recipientIdx = room.getUserA().getIdx().equals(senderIdx) ? room.getUserB().getIdx() : room.getUserA().getIdx();
        String preview = saved.content().length() > 30 ? saved.content().substring(0, 30) + "..." : saved.content();
        notificationService.notify(recipientIdx, senderIdx, NotificationType.CHAT_MESSAGE,
                sender.getName() + "님: " + preview, "/chats/" + roomIdx);

        return saved;
    }

    private ChatRoomEntity getRoomOrThrow(Long roomIdx, Long userIdx) {
        return chatRoomRepository.findById(roomIdx)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방", roomIdx));
    }

    private ChatRoomEntity checkParticipant(Long roomIdx, Long userIdx) {
        ChatRoomEntity room = getRoomOrThrow(roomIdx, userIdx);
        boolean isParticipant = room.getUserA().getIdx().equals(userIdx) || room.getUserB().getIdx().equals(userIdx);
        if (!isParticipant) {
            throw new ForbiddenException("본인이 참여 중인 채팅방만 볼 수 있습니다.");
        }
        return room;
    }
}
