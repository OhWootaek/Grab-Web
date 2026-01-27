package oh.grab_web.dto;

import oh.grab_web.domain.meeting.Meeting;
import oh.grab_web.domain.meeting.MeetingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Service 계층에서 사용하는 DTO(Data Transfer Object) 모음
 * record 사용, Immutable 데이터 정의
 */
public class ServiceDtos {

    // --- Meeting 관련 DTO ---

    public record MeetingCreateRequest(
            String title,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime
    ) {}

    public record MeetingResponse(
            Long id,
            String title,
            String code,
            String hostName,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
            MeetingStatus status,
            LocalDateTime confirmedDateTime
    ) {
        public static MeetingResponse from(Meeting meeting) {
            return new MeetingResponse(
                    meeting.getId(),
                    meeting.getTitle(),
                    meeting.getMeetingCode(),
                    meeting.getHost().getName(),
                    meeting.getStartDate(),
                    meeting.getEndDate(),
                    meeting.getStartTime(),
                    meeting.getEndTime(),
                    meeting.getStatus(),
                    meeting.getConfirmedDateTime()
            );
        }
    }

    // --- Vote(투표) 관련 DTO ---

    public record VoteRequest(
            List<LocalDateTime> slots // 사용자가 선택한 가능 시간 리스트
    ) {}

    public record HeatMapResponse(
            LocalDateTime slot,
            int count,
            List<String> availableMembers,
            int colorLevel // 0 ~ 5단계 (색상 농도 표시용)
    ) {}
}