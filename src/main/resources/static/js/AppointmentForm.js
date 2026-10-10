/* 예약 신청 폼: 장소란 <-> 지도 연동
   - 장소란에 입력 -> 지도에 후보 마커로 추천
   - 지도에서 마커 클릭 -> 장소란에 삽입 (필요하면 새 장소로 자동 등록) */
(function () {
    const mapEl = document.getElementById('apptMap');
    if (!mapEl || typeof kakao === 'undefined') return;

    const locationQuery = document.getElementById('locationQuery');
    const locationIdxInput = document.getElementById('locationIdxInput');
    const locationPicked = document.getElementById('locationPicked');
    const locationPickedText = document.getElementById('locationPickedText');
    const submitBtn = document.getElementById('apptSubmitBtn');

    const map = new kakao.maps.Map(mapEl, {
        center: new kakao.maps.LatLng(35.8714, 128.6014), // 대구 중심
        level: 5
    });
    const places = new kakao.maps.services.Places();
    let markers = [];
    let searchTimer = null;

    function clearMarkers() {
        markers.forEach(m => m.setMap(null));
        markers = [];
    }

    function clearSelection() {
        locationIdxInput.value = '';
        locationPicked.hidden = true;
        submitBtn.disabled = true;
    }

    function searchPlaces(keyword) {
        if (!keyword || !keyword.trim()) {
            clearMarkers();
            return;
        }
        places.keywordSearch(keyword, function (data, status) {
            if (status !== kakao.maps.services.Status.OK) {
                clearMarkers();
                return;
            }
            clearMarkers();
            const bounds = new kakao.maps.LatLngBounds();
            data.forEach(place => {
                const position = new kakao.maps.LatLng(place.y, place.x);
                const marker = new kakao.maps.Marker({ position, map });
                markers.push(marker);
                bounds.extend(position);

                kakao.maps.event.addListener(marker, 'click', function () {
                    selectPlace(place);
                });
            });
            map.setBounds(bounds);
        });
    }

    function selectPlace(place) {
        locationQuery.value = place.place_name;
        locationPickedText.textContent = place.road_address_name || place.address_name || '';
        locationPicked.hidden = false;
        submitBtn.disabled = true; // 등록 확정 전까지는 잠깐 비활성화

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
                locationIdxInput.value = loc.idx;
                submitBtn.disabled = false;
            })
            .catch(() => {
                alert('장소를 등록하지 못했어요. 다시 시도해 주세요.');
                clearSelection();
            });
    }

    // 장소란에 입력 -> 지도에 후보 추천 (입력 중엔 선택 무효화)
    locationQuery.addEventListener('input', function () {
        clearSelection();
        clearTimeout(searchTimer);
        searchTimer = setTimeout(() => searchPlaces(locationQuery.value), 300);
    });
    locationQuery.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            clearTimeout(searchTimer);
            searchPlaces(locationQuery.value);
        }
    });

    // 지도 영역 안의 검색창으로도 검색 가능 (마커 클릭 시 동작은 동일)
    const mapKeyword = document.getElementById('mapKeyword');
    const mapSearchBtn = document.getElementById('mapSearchBtn');
    if (mapKeyword && mapSearchBtn) {
        mapSearchBtn.addEventListener('click', () => searchPlaces(mapKeyword.value));
        mapKeyword.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                searchPlaces(mapKeyword.value);
            }
        });
    }

    window.addEventListener('resize', function () { map.relayout(); });
    window.addEventListener('load', function () { map.relayout(); });
})();
