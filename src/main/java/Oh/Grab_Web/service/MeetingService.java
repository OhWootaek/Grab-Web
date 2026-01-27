package oh.grab_web.service;

import lombok.RequiredArgsConstructor;
import oh.grab_web.domain.meeting.Meeting;
import oh.grab_web.domain.user.User;
import oh.grab_web.dto.ServiceDtos.MeetingCreateRequest;
import oh.grab_web.dto.ServiceDtos.MeetingResponse;
import oh.grab_web.repository.MeetingRepository;
import oh.grab_web.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    /**
     * 일정(방) 생성
     */
    @Transactional
    public String createMeeting(String hostEmail, MeetingCreateRequest request) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // UUID 생성 (URL 공유용)
        String meetingCode = UUID.randomUUID().toString();

        Meeting meeting = Meeting.builder()
                .host(host)
                .title(request.title())
                .meetingCode(meetingCode)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        meetingRepository.save(meeting);

        return meetingCode;
    }

    /**
     * 일정 조회 (UUID 이용)
     */
    public MeetingResponse getMeetingInfo(String meetingCode) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 일정 코드입니다."));

        return MeetingResponse.from(meeting);
    }

    /**
     * 일정 확정 (방장만 가능)
     */
    @Transactional
    public void confirmMeeting(String userEmail, String meetingCode, LocalDateTime confirmDateTime) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("회의를 찾을 수 없습니다."));

        // 권한 체크: 방장이 아니면 예외 발생
        if (!meeting.getHost().getEmail().equals(userEmail)) {
            throw new IllegalStateException("방장만 일정을 확정할 수 있습니다.");
        }

        meeting.confirmSchedule(confirmDateTime); // Dirty Checking으로 자동 Update
    }
}