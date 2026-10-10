function toggleTheme() {
    const body = document.body;
    const themeIcon = document.getElementById('themeIcon');
    body.classList.toggle('dark-mode');

    if (body.classList.contains('dark-mode')) {
        themeIcon.className = 'fa-solid fa-sun';
    } else {
        themeIcon.className = 'fa-solid fa-moon';
    }
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

function toggleCategory() {
    const subMenu = document.getElementById('categorySubMenu');
    const arrow = document.getElementById('categoryArrow');
    subMenu.classList.toggle('open');
    if (arrow) arrow.classList.toggle('open');
}

function toggleSubCategory(menuId, arrowId, event) {
    event.stopPropagation();
    const subMenu = document.getElementById(menuId);
    const arrow = document.getElementById(arrowId);
    subMenu.classList.toggle('open');
    if (arrow) arrow.classList.toggle('open');
}

/* ---- 실제 메시지 송수신 (폴링) ---- */
const chatContainer = document.getElementById('chatContainer');
const roomIdx = chatContainer.dataset.roomIdx;
const loginUserIdx = Number(chatContainer.dataset.loginUserIdx);
let lastMessageId = 0;
document.querySelectorAll('.msg-row[data-msg-id]').forEach(row => {
    lastMessageId = Math.max(lastMessageId, Number(row.dataset.msgId));
});

function formatTime(iso) {
    const d = new Date(iso);
    let hours = d.getHours();
    const minutes = d.getMinutes().toString().padStart(2, '0');
    const ampm = hours >= 12 ? '오후' : '오전';
    hours = hours % 12 || 12;
    return `${ampm} ${hours}:${minutes}`;
}

function appendMessage(msg) {
    if (Number(msg.idx) <= lastMessageId) return; // 이미 그려진 메시지(내가 보낸 직후 등) 중복 방지
    lastMessageId = Number(msg.idx);

    const isMe = Number(msg.senderIdx) === loginUserIdx;

    const msgRow = document.createElement('div');
    msgRow.className = isMe ? 'msg-row me' : 'msg-row other';
    msgRow.dataset.msgId = msg.idx;

    const bubbleGroup = document.createElement('div');
    bubbleGroup.className = 'msg-bubble-group';

    const msgBubble = document.createElement('div');
    msgBubble.className = 'msg-bubble';
    msgBubble.textContent = msg.content;

    const msgTime = document.createElement('span');
    msgTime.className = 'msg-time';
    msgTime.textContent = formatTime(msg.createdAt);

    bubbleGroup.appendChild(msgBubble);
    bubbleGroup.appendChild(msgTime);

    if (!isMe) {
        const img = document.createElement('img');
        img.src = '/images/cow.png';
        img.alt = '프로필';
        img.className = 'msg-profile-img';
        img.onerror = function () { this.src = 'https://cdn-icons-png.flaticon.com/512/847/847969.png'; };

        const wrap = document.createElement('div');
        wrap.className = 'msg-content-wrap';

        const name = document.createElement('span');
        name.className = 'msg-sender-name';
        name.textContent = msg.senderName;

        wrap.appendChild(name);
        wrap.appendChild(bubbleGroup);
        msgRow.appendChild(img);
        msgRow.appendChild(wrap);
    } else {
        msgRow.appendChild(bubbleGroup);
    }

    const emptyNotice = document.getElementById('chatEmpty');
    if (emptyNotice) emptyNotice.remove();

    chatContainer.appendChild(msgRow);
    chatContainer.scrollTop = chatContainer.scrollHeight;
}

async function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();
    if (content === "") return;

    input.value = "";
    closeAttachmentMenu();

    try {
        const res = await fetch(`/chats/${roomIdx}/messages`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ content })
        });
        if (res.status === 401 || res.url.includes('/login')) {
            alert('로그인이 필요합니다.');
            return;
        }
        if (!res.ok) {
            alert('메시지 전송에 실패했습니다.');
            return;
        }
        appendMessage(await res.json());
    } catch (e) {
        alert('메시지 전송 중 오류가 발생했습니다: ' + e.message);
    }
}

async function pollMessages() {
    try {
        const res = await fetch(`/chats/${roomIdx}/messages?afterId=${lastMessageId}`);
        if (!res.ok) return;
        const messages = await res.json();
        messages.forEach(appendMessage);
    } catch {
        // 네트워크 오류는 조용히 무시하고 다음 폴링에서 재시도
    }
}
setInterval(pollMessages, 3000);

function handleKeyPress(event) {
    if (event.key === 'Enter') sendMessage();
}

function toggleAttachmentMenu() {
    const menu = document.getElementById('attachmentMenu');
    const btn = document.getElementById('plusBtn');
    menu.classList.toggle('active');
    btn.classList.toggle('active');
}

function closeAttachmentMenu() {
    const menu = document.getElementById('attachmentMenu');
    const btn = document.getElementById('plusBtn');
    menu.classList.remove('active');
    btn.classList.remove('active');
}

function triggerFileInput(type) {
    const fileInput = document.getElementById('fileInput');
    if (type === 'image') fileInput.accept = "image/*";
    else if (type === 'video') fileInput.accept = "video/*";
    else fileInput.accept = "*/*";

    fileInput.click();
    closeAttachmentMenu();
}

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file) {
        alert(`'${file.name}' 파일이 선택되었습니다.`);
    }
}

/* 7. 예약 관련 JS 모듈 */

/* 7-1. 장소 지도 (검색하면 후보 마커 표시, 마커 클릭하면 장소란에 삽입) */
let chatApptMap = null;
let chatApptPlaces = null;
let chatApptMarkers = [];
let chatApptSearchTimer = null;

function initChatApptMapIfNeeded() {
    if (chatApptMap || typeof kakao === 'undefined') return;
    const mapEl = document.getElementById('chatApptMap');
    if (!mapEl) return;

    chatApptMap = new kakao.maps.Map(mapEl, {
        center: new kakao.maps.LatLng(35.8714, 128.6014), // 대구 중심
        level: 5
    });
    chatApptPlaces = new kakao.maps.services.Places();
}

function clearChatApptMarkers() {
    chatApptMarkers.forEach(m => m.setMap(null));
    chatApptMarkers = [];
}

function clearChatLocationSelection() {
    document.getElementById('locationIdxInput').value = '';
    document.getElementById('locationPicked').hidden = true;
}

function searchChatApptPlaces(keyword) {
    if (!chatApptPlaces) return;
    if (!keyword || !keyword.trim()) {
        clearChatApptMarkers();
        return;
    }
    chatApptPlaces.keywordSearch(keyword, function (data, status) {
        if (status !== kakao.maps.services.Status.OK) {
            clearChatApptMarkers();
            return;
        }
        clearChatApptMarkers();
        const bounds = new kakao.maps.LatLngBounds();
        data.forEach(place => {
            const position = new kakao.maps.LatLng(place.y, place.x);
            const marker = new kakao.maps.Marker({ position, map: chatApptMap });
            chatApptMarkers.push(marker);
            bounds.extend(position);
            kakao.maps.event.addListener(marker, 'click', function () {
                selectChatApptPlace(place);
            });
        });
        chatApptMap.setBounds(bounds);
    });
}

function selectChatApptPlace(place) {
    const locationQuery = document.getElementById('locationQuery');
    const locationPicked = document.getElementById('locationPicked');
    const locationPickedText = document.getElementById('locationPickedText');

    locationQuery.value = place.place_name;
    locationPickedText.textContent = place.road_address_name || place.address_name || '';
    locationPicked.hidden = false;

    fetch('/locations/resolve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            name: place.place_name,
            category: place.category_name,
            address: place.road_address_name || place.address_name,
            lat: Number(place.y),
            lng: Number(place.x)
        })
    })
        .then(res => {
            if (!res.ok) throw new Error('장소 등록에 실패했어요.');
            return res.json();
        })
        .then(loc => {
            document.getElementById('locationIdxInput').value = loc.idx;
        })
        .catch(() => {
            alert('장소를 등록하지 못했어요. 다시 시도해 주세요.');
            clearChatLocationSelection();
        });
}

(function setupChatLocationQuery() {
    const locationQuery = document.getElementById('locationQuery');
    if (!locationQuery) return;
    locationQuery.addEventListener('input', function () {
        clearChatLocationSelection();
        clearTimeout(chatApptSearchTimer);
        chatApptSearchTimer = setTimeout(() => searchChatApptPlaces(locationQuery.value), 300);
    });
    locationQuery.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            clearTimeout(chatApptSearchTimer);
            searchChatApptPlaces(locationQuery.value);
        }
    });

    // 지도 영역 안의 검색창으로도 검색 가능 (마커 클릭 시 동작은 동일)
    const mapKeyword = document.getElementById('mapKeyword');
    const mapSearchBtn = document.getElementById('mapSearchBtn');
    if (mapKeyword && mapSearchBtn) {
        mapSearchBtn.addEventListener('click', () => searchChatApptPlaces(mapKeyword.value));
        mapKeyword.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                searchChatApptPlaces(mapKeyword.value);
            }
        });
    }
})();

/* 7-2. 예약 모달 열기/닫기/전송 */
function openReservationModal() {
    closeAttachmentMenu();
    const modal = document.getElementById('reservationModal');
    modal.classList.add('active');

    // 기본 일시 설정 (1시간 후)
    const now = new Date();
    now.setHours(now.getHours() + 1);
    const isoString = new Date(now.getTime() - (now.getTimezoneOffset() * 60000)).toISOString().slice(0, 16);
    document.getElementById('reserveDateTime').value = isoString;

    // 모달이 숨겨진 상태에선 지도 크기를 잡을 수 없어서, 열릴 때 초기화/재배치한다
    initChatApptMapIfNeeded();
    if (chatApptMap) chatApptMap.relayout();
}

function closeReservationModal() {
    document.getElementById('reservationModal').classList.remove('active');
}

async function sendReservationMessage() {
    const dateTimeVal = document.getElementById('reserveDateTime').value;
    const mentorIdx = document.getElementById('mentorIdxInput').value.trim();
    const locationIdx = document.getElementById('locationIdxInput').value;
    const locationLabel = document.getElementById('locationQuery').value.trim();

    if (!dateTimeVal) { alert("약속 날짜와 시간을 선택해주세요."); return; }
    if (!mentorIdx)   { alert("멘토 회원 번호를 입력해주세요."); return; }
    if (!locationIdx) { alert("약속 장소를 지도에서 선택해주세요."); return; }

    // "2026-09-15T14:30" -> date="2026-09-15", time="14:30"
    const [date, time] = dateTimeVal.split('T');

    // 서버 @Future 제약: 예약 날짜는 오늘 이후만 허용
    const now = new Date();
    const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    if (date <= todayStr) { alert("약속 날짜는 내일 이후로 선택해주세요."); return; }

    const submitBtn = document.querySelector('.submit-reservation-btn');
    if (submitBtn) submitBtn.disabled = true;

    try {
        const res = await fetch('/appointments', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ mentorIdx, locationIdx, date, time })
        });

        if (res.url.includes('/login')) {
            alert("로그인이 필요합니다.");
            return;
        }
        if (res.url.includes('/appointments/new')) {
            alert("예약 정보가 올바르지 않습니다. 멘토 번호 · 장소 · 날짜(내일 이후) · 시간을 확인해주세요.");
            return;
        }
        if (!res.ok) {
            alert("예약 신청에 실패했습니다. (" + res.status + ")");
            return;
        }

        // 성공 -> 채팅방에 약속 제안 메시지를 실제로 남겨서 상대방도 폴링으로 보게 함
        const dt = new Date(dateTimeVal);
        const formattedDate = `${dt.getFullYear()}년 ${dt.getMonth() + 1}월 ${dt.getDate()}일 ${dt.getHours() >= 12 ? '오후' : '오전'} ${dt.getHours() % 12 || 12}:${dt.getMinutes().toString().padStart(2, '0')}`;
        const proposalText = `[멘토링 약속 제안] ${formattedDate} · ${locationLabel}`;

        const msgRes = await fetch(`/chats/${roomIdx}/messages`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ content: proposalText })
        });
        if (msgRes.ok) appendMessage(await msgRes.json());

        document.getElementById('locationQuery').value = '';
        clearChatLocationSelection();
        closeReservationModal();
    } catch (e) {
        alert("예약 신청 중 오류가 발생했습니다: " + e.message);
    } finally {
        if (submitBtn) submitBtn.disabled = false;
    }
}

window.onload = () => {
    chatContainer.scrollTop = chatContainer.scrollHeight;
};