package oh.grab_web.repository;

import oh.grab_web.domain.meeting.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    // URL 접속 시 UUID 코드로 방을 찾기 위한 메서드
    Optional<Meeting> findByMeetingCode(String meetingCode);
}