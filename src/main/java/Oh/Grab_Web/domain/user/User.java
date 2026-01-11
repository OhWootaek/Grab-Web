package Oh.Grab_Web.domain.user;

import Oh.Grab_Web.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 담는 Entity
 * OAuth2 로그인 시 제공받은 정보를 저장합니다.
 * Setter를 닫고 비즈니스 메서드(update)를 통해 상태를 변경합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 접근 권한을 protected로 제한하여 안전성 확보
@Table(name = "users") // user는 DB 예약어일 가능성이 높아 테이블명을 users로 지정
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String provider; // google, naver 등
    private String providerId; // 소셜 로그인 제공자 측의 고유 ID

    @Builder
    public User(String name, String email, String provider, String providerId) {
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
    }

    /**
     * 사용자 정보 수정 메서드
     * Dirty Checking을 통해 트랜잭션 종료 시 자동 업데이트 됩니다.
     */
    public User update(String name) {
        this.name = name;
        return this;
    }
}