package Oh.Grab_Web.domain.participant;

import Oh.Grab_Web.domain.meeting.Meeting;
import Oh.Grab_Web.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 참여자 정보를 담는 Entity (User <-> Meeting 다대다 연결)
 * 한 유저가 같은 방에 중복 참여하는 것을 DB Constraint로 방지합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_meeting_user",
                columnNames = {"meeting_id", "user_id"}
        ) // 중복 참여 방지 유니크 제약조건
})
public class Participant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role; // HOST(방장), GUEST(참여자)

    public Participant(Meeting meeting, User user, Role role) {
        this.meeting = meeting;
        this.user = user;
        this.role = role;
    }

    public enum Role {
        HOST, GUEST
    }
}