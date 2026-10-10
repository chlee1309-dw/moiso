// 클라이언트측: 비밀번호 일치 여부만 확인
document.getElementById('signupForm').addEventListener('submit', function (event) {
    const pw = document.getElementById('password').value;
    const pwCheck = document.getElementById('passwordCheck').value;

    if (pw !== pwCheck) {
        event.preventDefault();
        alert('비밀번호가 일치하지 않습니다.');
    }
});
