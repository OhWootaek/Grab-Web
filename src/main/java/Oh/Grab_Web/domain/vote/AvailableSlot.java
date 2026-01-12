package oh.grab_web.domain.vote;

import oh.grab_web.domain.meeting.Meeting;
import oh.grab_web.domain.participant.Participant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_slot_meeting", columnList = "meeting_id") // 미팅별 데이터 통계 집계용 인덱스
})
public class AvailableSlot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting; // 쿼리 최적화를 위한 역정규화 (Participant 타고 가는 것보다 빠름)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    private LocalDateTime dateTime; // 실제 가능한 시간 (예: 2024-01-20 10:00)

    public AvailableSlot(Meeting meeting, Participant participant, LocalDateTime dateTime) {
        this.meeting = meeting;
        this.participant = participant;
        this.dateTime = dateTime;
    }
}