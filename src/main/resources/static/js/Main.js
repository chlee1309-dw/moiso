// 오늘의 베스트 고민 클릭 시 베스트 게시글 페이지로 이동
function goToBestPost() {
    window.location.href = '/best-post';
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

function toggleCategory() {
    const submenu = document.getElementById('categorySubmenu');
    const arrow = document.getElementById('categoryArrow');
    submenu.classList.toggle('open');
    arrow.classList.toggle('rotate');
}

function toggleSubCategory(event, subMenuId) {
    event.stopPropagation();
    const subSubMenu = document.getElementById(subMenuId);
    const arrow = document.getElementById(subMenuId + 'Arrow');
    subSubMenu.classList.toggle('open');
    if (arrow) {
        arrow.classList.toggle('rotate');
    }
}

// --- 알림 팝업 토글 함수 ---
function toggleNotifPopup(event) {
    event.stopPropagation();
    const popup = document.getElementById('notifPopup');
    popup.classList.toggle('active');
}

// 바깥 영역 클릭 시 알림 팝업 닫기
document.addEventListener('click', (e) => {
    const popup = document.getElementById('notifPopup');
    if (popup && popup.classList.contains('active')) {
        popup.classList.remove('active');
    }
});

// 게시글 클릭 시 이동
function goToPost(event, url) {
    if (event.target.closest('.post-actions')) {
        return;
    }
    window.location.href = url;
}