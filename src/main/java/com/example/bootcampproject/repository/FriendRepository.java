package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.FriendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<FriendEntity, Long> {

    // 두 사용자 사이의 관계(방향 무관) - 요청/수락 여부 확인용
    @Query("select f from FriendEntity f where " +
            "(f.requester.idx = :userIdx and f.addressee.idx = :otherIdx) or " +
            "(f.requester.idx = :otherIdx and f.addressee.idx = :userIdx)")
    Optional<FriendEntity> findRelation(@Param("userIdx") Long userIdx, @Param("otherIdx") Long otherIdx);

    // 내가 받은 친구 요청 (대기 중)
    @Query("select f from FriendEntity f join fetch f.requester " +
            "where f.addressee.idx = :userIdx and f.status = 'REQUESTED' order by f.createdAt desc")
    List<FriendEntity> findIncomingRequests(@Param("userIdx") Long userIdx);

    // 내 친구 목록 (양방향, 수락된 것만)
    @Query("select f from FriendEntity f join fetch f.requester join fetch f.addressee " +
            "where (f.requester.idx = :userIdx or f.addressee.idx = :userIdx) and f.status = 'ACCEPTED' " +
            "order by f.createdAt desc")
    List<FriendEntity> findAcceptedByUserIdx(@Param("userIdx") Long userIdx);
}
