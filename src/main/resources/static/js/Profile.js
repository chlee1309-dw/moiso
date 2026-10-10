// 뒤로가기 (직접 접속 등 방문 기록이 없을 땐 메인으로)
function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = '/main';
    }
}

// 사이드바 제어 함수
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

// 마이페이지 "프로필 수정" 리스트 행을 누르면 폼을 펼치고/접는다 (사이드바 카테고리 화살표와 동일한 패턴)
function toggleProfileEdit() {
    const form = document.getElementById('profileEditForm');
    const arrow = document.getElementById('profileEditArrow');
    form.hidden = !form.hidden;
    arrow.classList.toggle('rotate');
}
