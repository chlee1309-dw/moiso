package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // 방의 전체 메시지 (보낸 사람까지 fetch join), 오래된 순
    @Query("select m from ChatMessageEntity m join fetch m.sender " +
            "where m.room.idx = :roomIdx order by m.idx asc")
    List<ChatMessageEntity> findByRoomIdxOrderByIdxAsc(@Param("roomIdx") Long roomIdx);

    // 폴링용 - 특정 메시지 idx 이후의 새 메시지만
    @Query("select m from ChatMessageEntity m join fetch m.sender " +
            "where m.room.idx = :roomIdx and m.idx > :afterIdx order by m.idx asc")
    List<ChatMessageEntity> findByRoomIdxAfter(@Param("roomIdx") Long roomIdx, @Param("afterIdx") Long afterIdx);

    // 채팅 목록 화면의 마지막 메시지 미리보기용
    Optional<ChatMessageEntity> findTopByRoom_IdxOrderByIdxDesc(Long roomIdx);
}
