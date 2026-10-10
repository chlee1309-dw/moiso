package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    // userA/userB는 항상 idx가 작은 쪽이 userA로 정렬되어 들어간다 (ChatService에서 보장)
    Optional<ChatRoomEntity> findByUserA_IdxAndUserB_Idx(Long userAIdx, Long userBIdx);

    // 내가 참여 중인 채팅방 전체 (상대방까지 fetch join)
    @Query("select r from ChatRoomEntity r " +
            "join fetch r.userA join fetch r.userB " +
            "where r.userA.idx = :userIdx or r.userB.idx = :userIdx")
    List<ChatRoomEntity> findMyRooms(@Param("userIdx") Long userIdx);
}
