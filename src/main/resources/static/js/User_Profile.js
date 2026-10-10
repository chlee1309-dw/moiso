// 뒤로가기 (직접 접속 등 방문 기록이 없을 땐 메인으로)
function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = '/main';
    }
}

// 사이드바 토글
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// 1단 카테고리 토글
function toggleCategory() {
    const submenu = document.getElementById('categorySubmenu');
    const arrow = document.getElementById('categoryArrow');
    submenu.classList.toggle('open');
    arrow.classList.toggle('rotate');
}

// 2단 서브 카테고리 토글
function toggleSubCategory(event, subMenuId) {
    event.stopPropagation();
    const subSubMenu = document.getElementById(subMenuId);
    const arrow = document.getElementById(subMenuId + 'Arrow');
    subSubMenu.classList.toggle('open');
    if (arrow) {
        arrow.classList.toggle('rotate');
    }
}
