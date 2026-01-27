package oh.grab_web.repository;

import oh.grab_web.domain.vote.AvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvailableSlotRepository extends JpaRepository<AvailableSlot, Long> {

    /**
     * 특정 참여자의 기존 투표 내역을 모두 삭제합니다.
     * @Modifying : DB 상태를 변경하는(Insert, Update, Delete) 쿼리임을 명시
     * @Query : JPQL을 직접 사용하여 Select 없이 바로 Delete 쿼리를 날려 성능을 최적화합니다.
     */
    @Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트 초기화 (데이터 불일치 방지)
    @Query("DELETE FROM AvailableSlot a WHERE a.participant.id = :participantId")
    void deleteByParticipantId(@Param("participantId") Long participantId);

    /**
     * 특정 미팅의 모든 투표 데이터를 조회합니다.
     * VoteService.getHeatMap() 메서드에서 전체 조회를 하지 않고, 해당 미팅 데이터만 가져오기 위함입니다.
     */
    List<AvailableSlot> findAllByMeetingId(Long meetingId);
}