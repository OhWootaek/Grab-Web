package oh.grab_web.repository;

import oh.grab_web.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 소셜 로그인 시 이메일로 회원을 찾기 위한 메서드
    Optional<User> findByEmail(String email);
}