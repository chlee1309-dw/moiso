document.addEventListener('DOMContentLoaded', () => {
    const notifBtn = document.getElementById('notifBtn');
    const notifDropdown = document.getElementById('notifDropdown');

    // 알림 버튼 클릭 시 팝업 토글
    notifBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        notifDropdown.classList.toggle('show');
    });

    // 팝업 내부 클릭 시 닫히지 않도록 이벤트 차단
    notifDropdown.addEventListener('click', (e) => {
        e.stopPropagation();
    });

    // 화면 다른 영역 클릭 시 팝업 닫기
    document.addEventListener('click', () => {
        if (notifDropdown.classList.contains('show')) {
            notifDropdown.classList.remove('show');
        }
    });
});