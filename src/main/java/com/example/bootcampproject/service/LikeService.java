package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.response.BestMentorResponse;
import com.example.bootcampproject.dto.response.BestQuestionResponse;
import com.example.bootcampproject.entity.AnswerEntity;
import com.example.bootcampproject.entity.AnswerLikeEntity;
import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.QuestionLikeEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.NotificationType;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final QuestionLikeRepository  questionLikeRepository;
    private final AnswerLikeRepository answerLikeRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository  questionRepository;
    private final NotificationService notificationService;

    @Transactional
    public void likeQuestion(Long userIdx, Long questionIdx) {
        QuestionEntity question = questionRepository.findById(questionIdx)
                .orElseThrow(() -> new ResourceNotFoundException("질문", questionIdx));

        if (questionLikeRepository.existsByUser_IdxAndQuestion_Idx(userIdx,questionIdx)) {
            return ;
        }
        QuestionLikeEntity like = new QuestionLikeEntity();
        like.setUser(userRepository.getReferenceById(userIdx));
        like.setQuestion(question);
        try {
            questionLikeRepository.save(like);
            notificationService.notify(question.getWriter().getIdx(), userIdx,
                    NotificationType.LIKE_QUESTION, "작성하신 질문에 좋아요가 눌렸습니다.", "/questions/" + questionIdx);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 unique 제약(uk_question_like_user_question)에 걸린 경우 - 이미 좋아요 상태이므로 무시
        }

    }

    @Transactional
    public void unlikeQuestion(Long userIdx, Long questionIdx) {
        questionLikeRepository.deleteByUser_IdxAndQuestion_Idx(userIdx,questionIdx);
    }

    @Transactional
    public boolean toggleQuestionLike(Long userIdx, Long questionIdx) {
        if (questionLikeRepository.existsByUser_IdxAndQuestion_Idx(userIdx,questionIdx)) {
            questionLikeRepository.deleteByUser_IdxAndQuestion_Idx(userIdx,questionIdx);
            return false;
        }
        likeQuestion(userIdx, questionIdx);
        return  true;
    }

    // ===== 답변(Answer) 좋아요 =====

    @Transactional
    public void likeAnswer(Long userIdx, Long answerIdx) {
        AnswerEntity answer = answerRepository.findById(answerIdx)
                .orElseThrow(() -> new ResourceNotFoundException("답변", answerIdx));

        if (answerLikeRepository.existsByUser_IdxAndAnswer_Idx(userIdx,answerIdx)) {
            return ;
        }
        AnswerLikeEntity like = new AnswerLikeEntity();
        like.setUser(userRepository.getReferenceById(userIdx));
        like.setAnswer(answer);
        try {
            answerLikeRepository.save(like);
            notificationService.notify(answer.getWriter().getIdx(), userIdx,
                    NotificationType.LIKE_ANSWER, "작성하신 답변에 좋아요가 눌렸습니다.",
                    "/questions/" + answer.getQuestion().getIdx());
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 unique 제약(uk_answer_like_user_answer)에 걸린 경우 - 이미 좋아요 상태이므로 무시
        }

    }

    @Transactional
    public void unlikeAnswer(Long userIdx, Long answerIdx) {
        answerLikeRepository.deleteByUser_IdxAndAnswer_Idx(userIdx,answerIdx);
    }

    @Transactional
    public boolean toggleAnswerLike(Long userIdx, Long answerIdx) {
        if (answerLikeRepository.existsByUser_IdxAndAnswer_Idx(userIdx,answerIdx)) {
            answerLikeRepository.deleteByUser_IdxAndAnswer_Idx(userIdx,answerIdx);
            return false;
        }
        likeAnswer(userIdx, answerIdx);
        return  true;
    }

    // ===== 조회 (상세/목록 응답 채우기) =====

    // 질문별 좋아요 수
    public long countQuestionLikes(Long questionIdx) {
        return questionLikeRepository.countByQuestion_Idx(questionIdx);
    }

    // 여러 질문의 좋아요 수 (questionIdx -> count). 좋아요 0개인 질문은 map에 없음
    public Map<Long, Long> countQuestionLikes(Collection<Long> questionIdxs) {
        if (questionIdxs == null || questionIdxs.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : questionLikeRepository.countByQuestionIdxIn(questionIdxs)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    // 답변별 좋아요 수
    public long countAnswerLikes(Long answerIdx) {
        return answerLikeRepository.countByAnswer_Idx(answerIdx);
    }

    // 여러 답변의 좋아요 수 (answerIdx -> count). 좋아요 0개인 답변은 map에 없음
    public Map<Long, Long> countAnswerLikes(Collection<Long> answerIdxs) {
        if (answerIdxs == null || answerIdxs.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : answerLikeRepository.countByAnswerIdxIn(answerIdxs)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    // 로그인 사용자가 이 질문에 좋아요를 눌렀는지 (버튼 상태)
    public boolean isQuestionLikedBy(Long userIdx, Long questionIdx) {
        return questionLikeRepository.existsByUser_IdxAndQuestion_Idx(userIdx, questionIdx);
    }

    // 로그인 사용자가 이 답변에 좋아요를 눌렀는지 (버튼 상태)
    public boolean isAnswerLikedBy(Long userIdx, Long answerIdx) {
        return answerLikeRepository.existsByUser_IdxAndAnswer_Idx(userIdx, answerIdx);
    }

    // 질문 목록에서 "내가 좋아요한 질문 idx" 한 번에 조회 (N+1 방지)
    public List<Long> findLikedQuestionIdxs(Long userIdx, Collection<Long> questionIdxs) {
        if (questionIdxs == null || questionIdxs.isEmpty()) {
            return List.of();
        }
        return questionLikeRepository.findLikedQuestionIdxs(userIdx, questionIdxs);
    }

    // 답변 목록에서 "내가 좋아요한 답변 idx" 한 번에 조회 (N+1 방지)
    public List<Long> findLikedAnswerIdxs(Long userIdx, Collection<Long> answerIdxs) {
        if (answerIdxs == null || answerIdxs.isEmpty()) {
            return List.of();
        }
        return answerLikeRepository.findLikedAnswerIdxs(userIdx, answerIdxs);
    }

    public List<BestQuestionResponse> getBestQuestions(int limit) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Object[]> rows = questionLikeRepository.findBestQuestionIdxBetween(start, end, PageRequest.of(0, limit));
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> orderedIdxs = new ArrayList<>(rows.size());
        Map<Long, Long> likeCountByIdx = new HashMap<>();
        for (Object[] row : rows) {
            Long questionIdx =  (Long) row[0];
            Long likeCount = (Long) row[1];
            orderedIdxs.add(questionIdx);
            likeCountByIdx.put(questionIdx, likeCount);
        }

        Map<Long, QuestionEntity> questionByIdx = questionRepository.findAllWithWriterByIdxIn(orderedIdxs).stream()
                .collect(Collectors.toMap(QuestionEntity::getIdx, q -> q));
        return orderedIdxs.stream()
                .map(questionByIdx::get)
                .filter(Objects::nonNull)
                .map(q -> BestQuestionResponse.from(q, likeCountByIdx.get(q.getIdx())))
                .toList();
    }

    public List<BestMentorResponse> getBestMentors(int limit) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<Object[]> rows = answerLikeRepository.findBestMentorIdxBetween(start, end, PageRequest.of(0, limit));
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> orderedIdxs = new ArrayList<>(rows.size());
        Map<Long, Long> likeCountByIdx = new HashMap<>();
        for (Object[] row : rows) {
            Long writerIdx = (Long) row[0];
            Long likeCount = (Long) row[1];
            orderedIdxs.add(writerIdx);
            likeCountByIdx.put(writerIdx, likeCount);
        }

        Map<Long, UserEntity> userByIdx = userRepository.findAllById(orderedIdxs).stream()
                .collect(Collectors.toMap(UserEntity::getIdx, u -> u));
        return orderedIdxs.stream()
                .map(userByIdx::get)
                .filter(Objects::nonNull)
                .map(u -> BestMentorResponse.from(u, likeCountByIdx.get(u.getIdx())))
                .toList();
    }
}
