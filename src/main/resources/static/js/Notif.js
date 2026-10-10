document.addEventListener('DOMContentLoaded', () => {
    const readAllBtn = document.getElementById('readAllBtn');
    const unreadCards = document.querySelectorAll('.notif-card.unread');

    readAllBtn.addEventListener('click', () => {
        unreadCards.forEach(card => {
            card.classList.remove('unread');
        });
    });
});