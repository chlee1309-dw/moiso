document.addEventListener("DOMContentLoaded", () => {

    /* ---- 알림 드롭다운 ---- */
    const notifBtn = document.getElementById('notifBtn');
    const notifDropdown = document.getElementById('notifDropdown');

    notifBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        notifDropdown.classList.toggle('show');
    });

    notifDropdown.addEventListener('click', (e) => {
        e.stopPropagation();
    });

    document.addEventListener('click', () => {
        if (notifDropdown.classList.contains('show')) {
            notifDropdown.classList.remove('show');
        }
    });

    /* ---- 달력 ---- */
    // key: 'YYYY-MM-DD', value: [{ time: 'HH:mm', title: '문자열' }, ...]
    // Calendar.html 이 th:inline="javascript" 로 서버 예약 데이터를 주입해 둠
    const events = window.CALENDAR_EVENTS || {};

    const today = new Date();
    let viewYear = today.getFullYear();
    let viewMonth = today.getMonth(); // 0-based
    let selectedKey = toKey(today);

    // ?date=YYYY-MM-DD 로 들어오면(일정 추가 후 리다이렉트 등) 해당 날짜를 바로 선택
    const requestedDate = new URLSearchParams(location.search).get('date');
    if (requestedDate && /^\d{4}-\d{2}-\d{2}$/.test(requestedDate)) {
        const [ry, rm, rd] = requestedDate.split('-').map(Number);
        viewYear = ry;
        viewMonth = rm - 1;
        selectedKey = requestedDate;
    }

    const titleEl = document.getElementById('calTitle');
    const gridEl = document.getElementById('calGrid');
    const selectedLabelEl = document.getElementById('calSelectedLabel');
    const emptyEl = document.getElementById('calEmpty');
    const listEl = document.getElementById('calEventList');
    const addEventBtn = document.getElementById('addEventBtn');
    const addEventForm = document.getElementById('addEventForm');
    const addEventDate = document.getElementById('addEventDate');

    function toKey(d) {
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    function renderCalendar() {
        titleEl.textContent = `${viewYear}년 ${viewMonth + 1}월`;
        gridEl.innerHTML = '';

        const firstDay = new Date(viewYear, viewMonth, 1);
        const startWeekday = firstDay.getDay(); // 0=일
        const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();
        const daysInPrevMonth = new Date(viewYear, viewMonth, 0).getDate();

        const cells = [];
        for (let i = startWeekday - 1; i >= 0; i--) {
            cells.push({
                day: daysInPrevMonth - i,
                outside: true,
                date: new Date(viewYear, viewMonth - 1, daysInPrevMonth - i)
            });
        }
        for (let d = 1; d <= daysInMonth; d++) {
            cells.push({ day: d, outside: false, date: new Date(viewYear, viewMonth, d) });
        }
        while (cells.length % 7 !== 0) {
            const d = cells.length - (startWeekday + daysInMonth) + 1;
            cells.push({ day: d, outside: true, date: new Date(viewYear, viewMonth + 1, d) });
        }

        cells.forEach(({ day, outside, date }) => {
            const key = toKey(date);
            const weekday = date.getDay();

            const cell = document.createElement('button');
            cell.type = 'button';
            cell.className = 'cal-day';
            if (outside) cell.classList.add('is-outside');
            if (weekday === 0) cell.classList.add('is-sun');
            if (weekday === 6) cell.classList.add('is-sat');
            if (toKey(today) === key) cell.classList.add('is-today');
            if (selectedKey === key) cell.classList.add('is-selected');

            const num = document.createElement('span');
            num.textContent = day;
            cell.appendChild(num);

            if (events[key]?.length) {
                const dot = document.createElement('span');
                dot.className = 'cal-dot';
                cell.appendChild(dot);
            }

            cell.addEventListener('click', () => {
                selectedKey = key;
                if (outside) {
                    viewYear = date.getFullYear();
                    viewMonth = date.getMonth();
                }
                renderCalendar();
            });

            gridEl.appendChild(cell);
        });

        renderEvents();
    }

    function renderEvents() {
        const [y, m, d] = selectedKey.split('-').map(Number);
        selectedLabelEl.textContent = `${y}년 ${m}월 ${d}일 일정`;
        addEventDate.value = selectedKey;

        const list = events[selectedKey] || [];
        listEl.innerHTML = '';
        emptyEl.hidden = list.length > 0;

        list.forEach(ev => {
            const item = document.createElement('div');
            item.className = 'cal-event-item';

            const time = document.createElement('span');
            time.className = 'cal-event-time';
            time.textContent = ev.time;
            item.appendChild(time);

            const title = document.createElement('span');
            title.className = 'cal-event-title';
            title.textContent = ev.title;
            item.appendChild(title);

            if (ev.removable) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = `/calendars/events/${ev.id}/delete`;
                form.className = 'cal-event-delete';

                const btn = document.createElement('button');
                btn.type = 'submit';
                btn.className = 'icon-btn';
                btn.title = '일정 삭제';
                btn.innerHTML = '<i class="fa-solid fa-xmark"></i>';
                form.appendChild(btn);

                item.appendChild(form);
            }

            listEl.appendChild(item);
        });
    }

    document.getElementById('calPrev').addEventListener('click', () => {
        viewMonth--;
        if (viewMonth < 0) { viewMonth = 11; viewYear--; }
        renderCalendar();
    });
    document.getElementById('calNext').addEventListener('click', () => {
        viewMonth++;
        if (viewMonth > 11) { viewMonth = 0; viewYear++; }
        renderCalendar();
    });

    renderCalendar();

    /* ---- 일정 직접 추가 ---- */
    addEventBtn.addEventListener('click', () => {
        addEventForm.hidden = !addEventForm.hidden;
    });

    /* ---- 캘린더 구독 링크 복사 ---- */
    const copyBtn = document.getElementById('copyFeedBtn');
    if (copyBtn) {
        copyBtn.addEventListener('click', async () => {
            const url = copyBtn.dataset.feedUrl;
            if (!url) return;
            try {
                await navigator.clipboard.writeText(url);
                const msg = document.getElementById('copyFeedMsg');
                msg.hidden = false;
                setTimeout(() => { msg.hidden = true; }, 2000);
            } catch {
                alert(url); // 클립보드 접근 실패 시 눈으로 보고 복사하도록
            }
        });
    }
});
