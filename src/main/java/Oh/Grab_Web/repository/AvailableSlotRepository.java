package oh.grab_web.repository;

import oh.grab_web.domain.vote.AvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailableSlotRepository extends JpaRepository<AvailableSlot, Long> {
    // 특정 미팅의 모든 시간 데이터를 한 번에 조회 (Fetch Join은 나중에 QueryDSL로 구현)
    // 지금은 기본 메서드만 있어도 충분합니다.
}