package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.request.AnswerCreateRequest;
import com.example.bootcampproject.dto.request.AnswerUpdateRequest;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.entity.AnswerEntity;
import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.NotificationType;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.AnswerRepository;
import com.example.bootcampproject.repository.QuestionRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Long create(Long questionIdx, Long writerIdx, AnswerCreateRequest request) {
        QuestionEntity question = questionRepository.findById(questionIdx)
                .orElseThrow(() -> new ResourceNotFoundException("질문", questionIdx));
        UserEntity user = userRepository.findById(writerIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", writerIdx));

        AnswerEntity parent = resolveParent(request.parentAnswerIdx(), question);
        AnswerEntity saved = answerRepository.save(request.toEntity(question, user, parent));

        String link = "/questions/" + questionIdx;
        if (parent != null) {
            notificationService.notify(parent.getWriter().getIdx(), writerIdx,
                    NotificationType.REPLY, user.getName() + "님이 답글을 남겼습니다.", link);
        } else {
            notificationService.notify(question.getWriter().getIdx(), writerIdx,
                    NotificationType.ANSWER, user.getName() + "님이 질문에 답변을 남겼습니다.", link);
        }
        return saved.getIdx();
    }

    @Transactional
    public void update(Long answerIdx, Long loginUserIdx,
                       AnswerUpdateRequest request) {
        AnswerEntity answer = getAnswerOrThrow(answerIdx);
        checkOwner(answer, loginUserIdx);
        request.applyTo(answer);
    }

    @Transactional
    public void delete(Long answerIdx, Long loginUserIdx) {
        AnswerEntity answer = getAnswerOrThrow(answerIdx);
        checkOwner(answer, loginUserIdx);
        answerRepository.delete(answer);
    }

    private AnswerEntity resolveParent(Long parentAnswerIdx, QuestionEntity question) {
        if (parentAnswerIdx == null) {
            return null;
        }
        AnswerEntity parent = getAnswerOrThrow(parentAnswerIdx);
        if (!parent.getQuestion().getIdx().equals(question.getIdx())) {
            throw new BusinessException("대상 답변이 이 질문에 포함되지 않습니다.");
        }
        return  parent;
    }

    private AnswerEntity getAnswerOrThrow(Long answerIdx) {
        return answerRepository.findById(answerIdx)
                .orElseThrow(() -> new ResourceNotFoundException("답변",answerIdx));
    }

    private void checkOwner(AnswerEntity answer, Long loginUserIdx) {
        if (!answer.getWriter().getIdx().equals(loginUserIdx)) {
            throw new ForbiddenException("본인이 작성한 답변만 수정 및 삭제할 수 있습니다.");
        }
    }
}

