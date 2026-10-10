function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = '/main';
    }
}

// 실제로 제출될 파일 목록. 미리보기에서 "x"로 지우면 여기서도 빠져야
// <input type="file">에 반영되므로, 선택된 파일을 이 배열로 직접 관리한다.
let selectedImageFiles = [];

function handleImageUpload(event) {
    const newFiles = Array.from(event.target.files);
    for (const file of newFiles) {
        if (selectedImageFiles.length >= 5) {
            alert('사진은 최대 5장까지 첨부할 수 있습니다.');
            break;
        }
        selectedImageFiles.push(file);
    }
    // 같은 파일을 다시 선택할 수 있도록 input 값 초기화
    event.target.value = '';
    syncFileInput();
    renderImagePreviews();
}

function removeImage(index) {
    selectedImageFiles.splice(index, 1);
    syncFileInput();
    renderImagePreviews();
}

// selectedImageFiles를 실제 <input type="file">의 FileList로 반영
function syncFileInput() {
    const dataTransfer = new DataTransfer();
    selectedImageFiles.forEach(file => dataTransfer.items.add(file));
    document.getElementById('fileInput').files = dataTransfer.files;
}

function renderImagePreviews() {
    const previewList = document.getElementById('previewList');
    const imgCount = document.getElementById('imgCount');
    previewList.innerHTML = '';

    selectedImageFiles.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const item = document.createElement('div');
            item.className = 'preview-item';
            item.innerHTML = `
                        <img src="${e.target.result}" alt="미리보기">
                        <div class="preview-remove-btn">
                            <i class="fa-solid fa-xmark"></i>
                        </div>
                    `;
            item.querySelector('.preview-remove-btn').addEventListener('click', () => removeImage(index));
            previewList.appendChild(item);
        };
        reader.readAsDataURL(file);
    });

    imgCount.innerText = selectedImageFiles.length;
}