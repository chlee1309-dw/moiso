(function () {
    var meta = document.querySelector('meta[name="_csrf"]');
    var token = meta ? meta.getAttribute('content') : null;
    if (!token) {
        return;
    }

    // fetch로 보내는 POST/PUT/PATCH/DELETE 요청에 자동으로 헤더를 붙인다 (Chat_Room.js 등).
    var originalFetch = window.fetch;
    if (originalFetch) {
        window.fetch = function (input, init) {
            init = init || {};
            var method = (init.method || 'GET').toUpperCase();
            if (method !== 'GET' && method !== 'HEAD') {
                var headers = new Headers(init.headers || {});
                if (!headers.has('X-CSRF-TOKEN')) {
                    headers.set('X-CSRF-TOKEN', token);
                }
                init.headers = headers;
            }
            return originalFetch(input, init);
        };
    }

    // 페이지에 있는 모든 method="post" 폼에 hidden _csrf 필드를 자동으로 넣는다.
    function injectIntoForms() {
        document.querySelectorAll('form').forEach(function (form) {
            var method = (form.getAttribute('method') || 'GET').toUpperCase();
            if (method === 'POST' && !form.querySelector('input[name="_csrf"]')) {
                var input = document.createElement('input');
                input.type = 'hidden';
                input.name = '_csrf';
                input.value = token;
                form.appendChild(input);
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', injectIntoForms);
    } else {
        injectIntoForms();
    }
})();
