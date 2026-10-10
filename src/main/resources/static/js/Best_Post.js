// 1. 안전한 뒤로가기 구현
function goBack() {
    if (document.referrer && document.referrer.indexOf(location.hostname) !== -1) {
        history.back();
    } else {
        window.location.href = '/main'; // 기본 돌아갈 페이지
    }
}

// 2. 좋아요 및 스크랩 상태 관리
let isLiked = false;
let likeCount = 48;

function toggleLike() {
    const likeBtn = document.getElementById('likeBtn');
    const likeIcon = document.getElementById('likeIcon');
    const likeText = document.getElementById('likeText');

    isLiked = !isLiked;
    if (isLiked) {
        likeCount++;
        likeBtn.classList.add('active');
        likeIcon.className = 'fa-solid fa-thumbs-up';
    } else {
        likeCount--;
        likeBtn.classList.remove('active');
        likeIcon.className = 'fa-regular fa-thumbs-up';
    }
    likeText.textContent = `좋아요 ${likeCount}`;
}

let isScrapped = false;
function toggleScrap() {
    const scrapBtn = document.getElementById('scrapBtn');
    const scrapIcon = document.getElementById('scrapIcon');

    isScrapped = !isScrapped;
    if (isScrapped) {
        scrapBtn.classList.add('active');
        scrapIcon.className = 'fa-solid fa-bookmark';
        alert('게시글이 스크랩되었습니다.');
    } else {
        scrapBtn.classList.remove('active');
        scrapIcon.className = 'fa-regular fa-bookmark';
    }
}

// 3. 실시간 댓글 등록 기능
let commentCount = 3;

function addComment() {
    const input = document.getElementById('commentInput');
    const text = input.value.trim();

    if (!text) {
        alert('댓글 내용을 입력해주세요.');
        input.focus();
        return;
    }

    const commentsCard = document.getElementById('commentsCard');

    // 새 댓글 HTML 구조 생성
    const commentItem = document.createElement('div');
    commentItem.className = 'comment-item';
    commentItem.innerHTML = `
                <div class="comment-header">
                    <span class="comment-author">나 (작성자)</span>
                    <span class="comment-date">방금 전</span>
                </div>
                <div class="comment-body">${escapeHtml(text)}</div>
            `;

    // 댓글 목록 하단에 추가
    commentsCard.appendChild(commentItem);

    // 댓글 수 업데이트
    commentCount++;
    document.getElementById('commentCountText').innerHTML = `<i class="fa-regular fa-comment"></i> 댓글 ${commentCount}`;

    // 입력창 초기화
    input.value = '';
}

// 엔터 키 처리
function handleKeyPress(event) {
    if (event.key === 'Enter') {
        addComment();
    }
}

// XSS 방지용 문자열 변환 함수
function escapeHtml(text) {
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}