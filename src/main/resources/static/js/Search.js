// 뒤로가기 (직접 접속 등 방문 기록이 없을 땐 메인으로)
function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = '/main';
    }
}

const searchForm = document.getElementById('searchForm');
const searchBox = document.getElementById('searchBox');
const clearBtn = document.getElementById('clearBtn');
const recentSection = document.getElementById('recentSection');
const recentTags = document.getElementById('recentTags');

const MAX_RECENT = 5;

// 브라우저 로컬 스토리지에서 기존 검색어 가져오기
let recentSearches = JSON.parse(localStorage.getItem('recentSearches')) || [];

// 초기 화면 렌더링
updateRecentUI();
toggleClearBtn();

/* [기능 1] 검색창 실시간 타이핑 효과 및 삭제(X) 버튼 제어 */
searchBox.addEventListener('input', toggleClearBtn);

function toggleClearBtn() {
    clearBtn.style.display = searchBox.value.trim().length > 0 ? 'flex' : 'none';
}

// X 버튼 누르면 검색창 초기화
clearBtn.addEventListener('click', () => {
    searchBox.value = '';
    toggleClearBtn();
    searchBox.focus();
});

/* [기능 2] 검색 실행 (빈 검색어는 막고, 저장 후 전송) */
searchForm.addEventListener('submit', (e) => {
    const keyword = searchBox.value.trim();
    if (!keyword) {
        e.preventDefault();
        searchBox.focus();
        return;
    }
    searchBox.value = keyword;
    saveSearch(keyword);
});

/* [기능 3] 추천 키워드 / 최근 검색어 클릭 시 바로 검색 실행 */
document.querySelectorAll('.keyword-item').forEach(item => {
    item.addEventListener('click', () => selectKeyword(item.textContent.trim()));
});

function selectKeyword(keyword) {
    searchBox.value = keyword;
    toggleClearBtn();
    // requestSubmit 은 submit 이벤트를 거쳐 저장까지 처리됨
    if (searchForm.requestSubmit) {
        searchForm.requestSubmit();
    } else {
        saveSearch(keyword);
        searchForm.submit();
    }
}

/* [기능 4] 최근 검색어 저장 및 삭제 로직 */
function saveSearch(keyword) {
    // 중복 제거 후 최신 검색어를 맨 앞으로
    recentSearches = [keyword, ...recentSearches.filter(item => item !== keyword)]
        .slice(0, MAX_RECENT);

    localStorage.setItem('recentSearches', JSON.stringify(recentSearches));
    updateRecentUI();
}

// 최근 검색어 삭제
function deleteSearch(keyword) {
    recentSearches = recentSearches.filter(item => item !== keyword);
    localStorage.setItem('recentSearches', JSON.stringify(recentSearches));
    updateRecentUI();
}

// 최근 검색어 UI 업데이트
function updateRecentUI() {
    recentTags.innerHTML = '';

    if (recentSearches.length === 0) {
        recentSection.style.display = 'none';
        return;
    }

    recentSection.style.display = 'block';
    recentSearches.forEach(keyword => {
        const badge = document.createElement('div');
        badge.className = 'recent-badge';

        // XSS 방지를 위해 textContent 사용
        const label = document.createElement('span');
        label.textContent = keyword;
        label.addEventListener('click', () => selectKeyword(keyword));

        const removeBtn = document.createElement('button');
        removeBtn.type = 'button';
        removeBtn.className = 'delete-btn';
        removeBtn.textContent = '✕';
        removeBtn.addEventListener('click', (event) => {
            event.stopPropagation();  // 배지 클릭(검색 실행) 방지
            deleteSearch(keyword);
        });

        badge.append(label, removeBtn);
        recentTags.appendChild(badge);
    });
}
