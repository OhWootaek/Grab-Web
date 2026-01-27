document.addEventListener('DOMContentLoaded', () => {
    initGrid();
    loadHeatMap();
});

let isMouseDown = false; // 드래그 상태 확인용
let selectedSlots = new Set(); // 내가 선택한 시간들 (Set으로 중복 방지)

// 1. 그리드 초기화 및 생성
function initGrid() {
    const container = document.getElementById('time-grid-container');
    const grid = document.createElement('div');
    grid.id = 'time-grid';

    // 날짜/시간 파싱
    const startD = new Date(meetingData.startDate);
    const endD = new Date(meetingData.endDate);
    const startH = parseInt(meetingData.startTime.split(':')[0]);
    const endH = parseInt(meetingData.endTime.split(':')[0]);

    // 날짜 배열 생성
    const dates = [];
    for(let d = new Date(startD); d <= endD; d.setDate(d.getDate() + 1)) {
        dates.push(new Date(d));
    }

    // CSS Grid 컬럼 설정 (시간라벨열 + 날짜열들)
    grid.style.gridTemplateColumns = `60px repeat(${dates.length}, 1fr)`;

    // 헤더 생성 (빈칸 + 날짜들)
    grid.appendChild(createCell('grid-header', '')); // 왼쪽 위 빈칸
    dates.forEach(date => {
        const label = `${date.getMonth()+1}/${date.getDate()} (${getDayName(date)})`;
        grid.appendChild(createCell('grid-header', label));
    });

    // 시간 슬롯 생성 (30분 단위)
    // 예: 09:00, 09:30, 10:00 ...
    for (let h = startH; h < endH; h++) {
        for (let m of [0, 30]) {
            // 시간 라벨 (왼쪽 열)
            const timeLabel = `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}`;
            grid.appendChild(createCell('grid-time-label', timeLabel));

            // 각 날짜별 슬롯 생성
            dates.forEach(date => {
                const cell = document.createElement('div');
                cell.className = 'time-slot level-0'; // 기본: 불가능(회색)

                // ISO Data Attribute 설정 (서버 전송용)
                // 포맷: 2024-01-20T09:30:00
                const isoTime = makeIsoString(date, h, m);
                cell.dataset.time = isoTime;

                // 이벤트 리스너 등록 (드래그)
                addDragListeners(cell);

                grid.appendChild(cell);
            });
        }
    }

    container.appendChild(grid);
}

// 2. 드래그 이벤트 처리
function addDragListeners(cell) {
    cell.addEventListener('mousedown', (e) => {
        e.preventDefault(); // 텍스트 선택 방지
        isMouseDown = true;
        toggleSlot(cell);
    });

    cell.addEventListener('mouseenter', () => {
        if (isMouseDown) {
            toggleSlot(cell);
        }
    });

    // 화면 밖에서 마우스 뗐을 때 처리
    document.addEventListener('mouseup', () => {
        isMouseDown = false;
    });
}

// 슬롯 선택/해제 토글
function toggleSlot(cell) {
    const time = cell.dataset.time;
    if (selectedSlots.has(time)) {
        selectedSlots.delete(time);
        cell.classList.remove('my-pick'); // 체크 표시 제거
        cell.classList.remove('selected'); // 테두리 제거
    } else {
        selectedSlots.add(time);
        cell.classList.add('my-pick');
        cell.classList.add('selected');
    }
    updateSelectedCount();
}

// 3. 서버 통신 (투표 제출)
document.getElementById('btn-submit').addEventListener('click', () => {
    const slots = Array.from(selectedSlots);

    fetch(`/api/votes/${meetingData.code}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ slots: slots })
    })
        .then(res => {
            if (res.ok) {
                alert('투표가 저장되었습니다! 🎉');
                loadHeatMap(); // 최신 상태로 새로고침
            } else {
                alert('오류가 발생했습니다.');
            }
        });
});

// 4. 서버 통신 (히트맵 로드)
function loadHeatMap() {
    fetch(`/api/votes/${meetingData.code}`)
        .then(res => res.json())
        .then(data => {
            // 모든 슬롯 초기화
            document.querySelectorAll('.time-slot').forEach(el => {
                el.className = 'time-slot level-0'; // 레벨 초기화
                // 내 선택(my-pick)은 유지해야 함
                if (selectedSlots.has(el.dataset.time)) {
                    el.classList.add('my-pick');
                    el.classList.add('selected');
                }
            });

            // 데이터 적용
            data.forEach(info => {
                // info: { slot: "...", count: 3, colorLevel: 5, availableMembers: [...] }
                const cell = document.querySelector(`.time-slot[data-time="${info.slot}"]`);
                if (cell) {
                    cell.classList.remove('level-0');
                    cell.classList.add(`level-${info.colorLevel}`);
                    cell.title = `가능한 사람: ${info.availableMembers.join(', ')}`; // 호버 시 이름 표시
                }
            });
        });
}

// 유틸리티 함수들
function createCell(className, text) {
    const div = document.createElement('div');
    div.className = className;
    div.textContent = text;
    return div;
}

function getDayName(date) {
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    return days[date.getDay()];
}

function makeIsoString(date, h, m) {
    const y = date.getFullYear();
    const mo = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    const hh = String(h).padStart(2, '0');
    const mm = String(m).padStart(2, '0');
    return `${y}-${mo}-${d}T${hh}:${mm}:00`;
}

function updateSelectedCount() {
    document.getElementById('selected-count').textContent = selectedSlots.size;
}

function copyLink() {
    const url = window.location.href;
    navigator.clipboard.writeText(url).then(() => {
        alert('링크가 복사되었습니다! 팀원들에게 공유하세요.');
    });
}