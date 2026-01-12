package oh.grab_web.domain.meeting;

import oh.grab_web.domain.BaseTimeEntity;
import oh.grab_web.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 일정(방) 정보를 담는 Entity
 * URL 공유를 위해 UUID(meetingCode)를 사용하며, 조회 성능을 위해 Index를 설정했습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meeting", indexes = {
        @Index(name = "idx_meeting_code", columnList = "meeting_code") // UUID로 조회 시 성능 향상
})
public class Meeting extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 설정하여 불필요한 조인 방지
    @JoinColumn(name = "host_id")
    private User host; // 방장 (생성자)

    @Column(nullable = false)
    private String title;

    // URL에 노출될 난수 코드 (PK 노출 방지)
    @Column(nullable = false, unique = true, name = "meeting_code")
    private String meetingCode;

    // 일정 범위 (날짜)
    private LocalDate startDate;
    private LocalDate endDate;

    // 일정 범위 (시간)
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private MeetingStatus status; // OPEN(진행중), CONFIRMED(확정됨)

    private LocalDateTime confirmedDateTime; // 최종 확정된 시간

    @Builder
    public Meeting(User host, String title, String meetingCode, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
        this.host = host;
        this.title = title;
        this.meetingCode = meetingCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = MeetingStatus.OPEN;
    }

    /**
     * 일정 확정 비즈니스 로직
     */
    public void confirmSchedule(LocalDateTime confirmedDateTime) {
        this.status = MeetingStatus.CONFIRMED;
        this.confirmedDateTime = confirmedDateTime;
    }
}