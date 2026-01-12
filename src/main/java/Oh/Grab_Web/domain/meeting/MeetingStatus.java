package oh.grab_web.domain.meeting;

/**
 * 미팅의 진행 상태를 관리하는 Enum
 * OPEN: 투표 진행 중
 * CONFIRMED: 투표 마감 및 날짜 확정
 */
public enum MeetingStatus {
    OPEN,      // 진행 중
    CONFIRMED  // 확정됨
}