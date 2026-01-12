package oh.grab_web.repository;

import oh.grab_web.domain.participant.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    // 특정 유저가 특정 미팅에 이미 참여했는지 확인 (중복 방지 로직용)
    boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);
}