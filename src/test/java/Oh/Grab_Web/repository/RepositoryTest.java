package oh.grab_web.repository;

import oh.grab_web.GrabWebApplication;

import oh.grab_web.domain.meeting.Meeting;
import oh.grab_web.domain.meeting.MeetingStatus;
import oh.grab_web.domain.participant.Participant;
import oh.grab_web.domain.user.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ContextConfiguration(classes = GrabWebApplication.class)
public class RepositoryTest { // public 추가

    @Autowired UserRepository userRepository;
    @Autowired MeetingRepository meetingRepository;
    @Autowired ParticipantRepository participantRepository;

    @Configuration
    @EnableJpaAuditing
    static class TestConfig {}

    @Test
    @DisplayName("1. BaseTimeEntity 동작 확인: 유저 저장 시 생성시간이 자동 등록된다")
    public void auditingTest() { // public 추가
        User user = User.builder()
                .name("테스터")
                .email("test@gmail.com")
                .provider("google")
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getCreatedAt()).isNotNull();
        System.out.println("생성 시간: " + savedUser.getCreatedAt());
    }

    @Test
    @DisplayName("2. Meeting 저장 및 Enum 매핑 확인")
    public void meetingSaveTest() { // public 추가
        User host = userRepository.save(User.builder().name("방장").email("host@gmail.com").build());

        Meeting meeting = Meeting.builder()
                .host(host)
                .title("졸업 프로젝트 회의")
                .meetingCode(UUID.randomUUID().toString())
                .startDate(LocalDate.of(2024, 1, 20))
                .endDate(LocalDate.of(2024, 1, 25))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        Meeting savedMeeting = meetingRepository.save(meeting);

        assertThat(savedMeeting.getStatus()).isEqualTo(MeetingStatus.OPEN);
        assertThat(savedMeeting.getTitle()).isEqualTo("졸업 프로젝트 회의");
    }

    @Test
    @DisplayName("3. Unique Constraint 확인: 동일한 유저가 같은 방에 중복 참여하면 예외가 발생한다")
    public void uniqueConstraintTest() { // public 추가
        User user = userRepository.save(User.builder().name("참여자").email("guest@gmail.com").build());

        Meeting meeting = meetingRepository.save(Meeting.builder()
                .host(user)
                .title("중복 참여 테스트")
                .meetingCode(UUID.randomUUID().toString())
                .build());

        participantRepository.save(new Participant(meeting, user, Participant.Role.GUEST));

        assertThrows(DataIntegrityViolationException.class, () -> {
            participantRepository.save(new Participant(meeting, user, Participant.Role.GUEST));
        });
    }
}