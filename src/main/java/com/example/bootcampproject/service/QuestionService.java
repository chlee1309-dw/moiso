package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.request.QuestionCreateRequest;
import com.example.bootcampproject.dto.request.QuestionUpdateRequest;
import com.example.bootcampproject.dto.response.AnswerResponse;
import com.example.bootcampproject.dto.response.QuestionDetailResponse;
import com.example.bootcampproject.dto.response.QuestionListItemResponse;
import com.example.bootcampproject.entity.AnswerEntity;
import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.QuestionImageEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.QuestionCategory;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.AnswerRepository;
import com.example.bootcampproject.repository.QuestionImageRepository;
import com.example.bootcampproject.repository.QuestionRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    // 질문 하나에 첨부 가능한 사진 최대 장수
    private static final int MAX_IMAGES_PER_QUESTION = 5;

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuestionImageRepository questionImageRepository;
    private final LikeService likeService;
    private final FileStorageService fileStorageService;
    private final ReviewService reviewService;

    @Transactional
    public Long create(Long writerIdx, QuestionCreateRequest request, List<MultipartFile> images) {
        UserEntity writer = userRepository.findById(writerIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", writerIdx));

        QuestionEntity saved = questionRepository.save(request.toEntity(writer));
        saveImages(saved, images);
        return saved.getIdx();
    }

    @Transactional
    public void delete(Long questionIdx, Long loginUserIdx) {
        QuestionEntity question = getQuestionOrThrow(questionIdx);
        checkOwner(question, loginUserIdx);

        List<QuestionImageEntity> images = questionImageRepository.findByQuestion_IdxOrderByDisplayOrderAsc(questionIdx);
        questionImageRepository.deleteByQuestion_Idx(questionIdx);
        questionRepository.delete(question);
        images.forEach(image -> fileStorageService.delete(image.getImagePath()));
    }

    @Transactional
    public void update(Long questionIdx, Long loginUserIdx, QuestionUpdateRequest request, List<MultipartFile> images) {
        QuestionEntity question = getQuestionOrThrow(questionIdx);
        checkOwner(question, loginUserIdx);
        request.applyTo(question);
        saveImages(question, images);
    }

    // 새로 첨부된 사진들을 저장하고, 기존 사진 뒤에 순서대로 이어붙인다.
    private void saveImages(QuestionEntity question, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        List<MultipartFile> realFiles = images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
        if (realFiles.isEmpty()) {
            return;
        }

        long existingCount = questionImageRepository.countByQuestion_Idx(question.getIdx());
        if (existingCount + realFiles.size() > MAX_IMAGES_PER_QUESTION) {
            throw new BusinessException("사진은 최대 " + MAX_IMAGES_PER_QUESTION + "장까지 첨부할 수 있습니다.");
        }

        int order = (int) existingCount;
        for (MultipartFile file : realFiles) {
            String path = fileStorageService.storeQuestionImage(file);
            QuestionImageEntity image = QuestionImageEntity.builder()
                    .imagePath(path)
                    .displayOrder(order++)
                    .build();
            image.setQuestion(question);
            questionImageRepository.save(image);
        }
    }

    // 질문 목록 카테고리/키워드로 선택적 필터링
    public Page<QuestionListItemResponse> getList(QuestionCategory category, String keyword, Pageable pageable) {
        boolean hasKeyword = StringUtils.hasText(keyword);
        Page<QuestionEntity> page;
        if (category != null && hasKeyword) {
            page = questionRepository.findByCategoryAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(category, keyword, pageable);
        } else if (category != null) {
            page = questionRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        } else if (hasKeyword) {
            page = questionRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(keyword, pageable);
        } else {
            page = questionRepository.findByOrderByCreatedAtDesc(pageable);
        }
        // 페이지 내 질문들의 좋아요 수 / 썸네일을 쿼리 한 번으로 일괄 조회 (N+1 방지)
        List<Long> questionIdxs = page.getContent().stream().map(QuestionEntity::getIdx).toList();
        Map<Long, Long> likeCounts = likeService.countQuestionLikes(questionIdxs);
        Map<Long, String> thumbnails = findThumbnails(questionIdxs);

        return page.map(q -> QuestionListItemResponse.from(
                q,
                answerRepository.countByQuestion_Idx(q.getIdx()),
                likeCounts.getOrDefault(q.getIdx(), 0L),
                thumbnails.get(q.getIdx())));
    }

    // 메인 화면 - 최근 게시물 (전체 사용자)
    public List<QuestionListItemResponse> getRecent(int limit) {
        List<QuestionEntity> questions = questionRepository
                .findByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .getContent();
        return toListItems(questions);
    }

    // 메인 화면 - 내가 작성한 게시물
    public List<QuestionListItemResponse> getMyQuestions(Long writerIdx, int limit) {
        List<QuestionEntity> questions = questionRepository.findByWriter_IdxOrderByCreatedAtDesc(writerIdx);
        if (questions.size() > limit) {
            questions = questions.subList(0, limit);
        }
        return toListItems(questions);
    }

    private List<QuestionListItemResponse> toListItems(List<QuestionEntity> questions) {
        List<Long> questionIdxs = questions.stream().map(QuestionEntity::getIdx).toList();
        Map<Long, Long> likeCounts = likeService.countQuestionLikes(questionIdxs);
        Map<Long, String> thumbnails = findThumbnails(questionIdxs);
        return questions.stream()
                .map(q -> QuestionListItemResponse.from(
                        q,
                        answerRepository.countByQuestion_Idx(q.getIdx()),
                        likeCounts.getOrDefault(q.getIdx(), 0L),
                        thumbnails.get(q.getIdx())))
                .toList();
    }

    // 여러 질문의 첫 번째 첨부 사진 (questionIdx -> 이미지 경로). 사진 없는 질문은 map에 없음
    private Map<Long, String> findThumbnails(Collection<Long> questionIdxs) {
        if (questionIdxs == null || questionIdxs.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> thumbnails = new HashMap<>();
        for (Object[] row : questionImageRepository.findFirstImagesByQuestionIdxs(questionIdxs)) {
            thumbnails.put((Long) row[0], (String) row[1]);
        }
        return thumbnails;
    }

    public QuestionDetailResponse getDetail(Long questionIdx, Long loginUserIdx) {
        QuestionEntity question = questionRepository.findDetailByIdx(questionIdx)
                .orElseThrow(() -> new ResourceNotFoundException("질문", questionIdx));

        List<AnswerEntity> answerEntities = answerRepository.findByQuestionIdxWithWriter(questionIdx);
        List<Long> answerIdxs = answerEntities.stream().map(AnswerEntity::getIdx).toList();

        // 답변 좋아요 수/여부를 각각 쿼리 한 번으로 일괄 조회 (N+1 방지)
        Map<Long, Long> answerLikeCounts = likeService.countAnswerLikes(answerIdxs);
        Set<Long> likedAnswerIdxs = loginUserIdx != null
                ? new HashSet<>(likeService.findLikedAnswerIdxs(loginUserIdx, answerIdxs))
                : Set.of();
        Set<Long> reviewedAnswerIdxs = reviewService.findReviewedAnswerIdxs(loginUserIdx, answerIdxs);

        List<AnswerResponse> flatAnswers = answerEntities.stream()
                .map(a -> AnswerResponse.from(
                        a,
                        answerLikeCounts.getOrDefault(a.getIdx(), 0L),
                        likedAnswerIdxs.contains(a.getIdx()),
                        reviewedAnswerIdxs.contains(a.getIdx())))
                .toList();
        List<AnswerResponse> answerTree = AnswerResponse.toTree(flatAnswers);

        long likeCount = likeService.countQuestionLikes(questionIdx);
        boolean likedByMe = loginUserIdx != null
                && likeService.isQuestionLikedBy(loginUserIdx, questionIdx);

        List<String> imageUrls = questionImageRepository.findByQuestion_IdxOrderByDisplayOrderAsc(questionIdx).stream()
                .map(QuestionImageEntity::getImagePath)
                .toList();

        return QuestionDetailResponse.from(question, answerTree, flatAnswers.size(), imageUrls, likeCount, likedByMe);
    }

    private QuestionEntity getQuestionOrThrow(Long questionIdx) {
        return questionRepository.findById(questionIdx)
                .orElseThrow(() -> new ResourceNotFoundException("질문", questionIdx));
    }

    private void checkOwner(QuestionEntity question, Long loginUserIdx) {
        if (!question.getWriter().getIdx().equals(loginUserIdx)) {
            throw new ForbiddenException("본인이 작성한 질문만 수정 및 삭제할 수 있습니다.");
        }
    }

}
