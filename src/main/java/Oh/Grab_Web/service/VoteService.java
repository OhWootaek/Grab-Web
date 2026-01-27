package oh.grab_web.service;

import lombok.RequiredArgsConstructor;
import oh.grab_web.domain.meeting.Meeting;
import oh.grab_web.domain.participant.Participant;
import oh.grab_web.domain.participant.Participant.Role;
import oh.grab_web.domain.user.User;
import oh.grab_web.domain.vote.AvailableSlot;
import oh.grab_web.dto.ServiceDtos.HeatMapResponse;
import oh.grab_web.repository.AvailableSlotRepository;
import oh.grab_web.repository.MeetingRepository;
import oh.grab_web.repository.ParticipantRepository;
import oh.grab_web.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final AvailableSlotRepository availableSlotRepository;

    /**
     * 모임 참여 (최초 접속 시)
     * 이미 참여한 유저라면 아무것도 하지 않고, 처음이면 Participant 데이터를 생성합니다.
     */
    @Transactional
    public void enterMeeting(String userEmail, String meetingCode) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));

        // 이미 참여자인지 확인 (중복 Insert 방지)
        if (participantRepository.existsByMeetingIdAndUserId(meeting.getId(), user.getId())) {
            return;
        }

        // 방장이면 HOST, 아니면 GUEST
        Role role = meeting.getHost().getId().equals(user.getId()) ? Role.HOST : Role.GUEST;

        participantRepository.save(new Participant(meeting, user, role));
    }

    /**
     * 시간 투표 제출 (수정 포함)
     * 기존 투표 내역을 Bulk Delete로 한 번에 지우고, 새로 선택한 시간을 저장합니다.
     */
    @Transactional
    public void submitVote(String userEmail, String meetingCode, List<LocalDateTime> slots) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        // 참여자 정보 가져오기
        // (추후 ParticipantRepository에 findByMeetingIdAndUserId 추가 권장)
        Participant participant = participantRepository.findAll().stream()
                .filter(p -> p.getMeeting().getId().equals(meeting.getId()) && p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("참여자가 아닙니다."));

        // 1. 기존 투표 내역 삭제 (최적화됨: Bulk Delete Query 실행)
        availableSlotRepository.deleteByParticipantId(participant.getId());

        // 2. 새로운 투표 내역 저장 (Bulk Insert)
        List<AvailableSlot> newSlots = slots.stream()
                .map(time -> new AvailableSlot(meeting, participant, time))
                .collect(Collectors.toList());

        availableSlotRepository.saveAll(newSlots);
    }

    /**
     * 히트맵 데이터 계산 (핵심 알고리즘)
     * 시간대별로 몇 명이 가능한지, 누가 가능한지 집계하여 반환합니다.
     */
    public List<HeatMapResponse> getHeatMap(String meetingCode) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        // 1. 해당 미팅의 모든 투표 데이터 조회 (최적화됨: 특정 미팅 데이터만 조회)
        List<AvailableSlot> allSlots = availableSlotRepository.findAllByMeetingId(meeting.getId());

        // 2. 전체 참여자 수 계산 (색상 레벨 계산용)
        long totalParticipants = participantRepository.count(); // TODO: countByMeetingId 로 변경 필요

        // 3. 시간대별 그룹핑 (Time -> List<User>)
        Map<LocalDateTime, List<String>> timeToUserMap = allSlots.stream()
                .collect(Collectors.groupingBy(
                        AvailableSlot::getDateTime,
                        Collectors.mapping(slot -> slot.getParticipant().getUser().getName(), Collectors.toList())
                ));

        // 4. DTO 변환
        List<HeatMapResponse> responses = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<String>> entry : timeToUserMap.entrySet()) {
            int count = entry.getValue().size();

            // 색상 레벨 계산 (0~5단계)
            int level = calculateColorLevel(count, (int) totalParticipants);

            responses.add(new HeatMapResponse(
                    entry.getKey(),
                    count,
                    entry.getValue(),
                    level
            ));
        }

        return responses;
    }

    private int calculateColorLevel(int count, int total) {
        if (total == 0) return 0;
        double ratio = (double) count / total;
        if (ratio >= 0.8) return 5; // 80% 이상
        if (ratio >= 0.6) return 4;
        if (ratio >= 0.4) return 3;
        if (ratio >= 0.2) return 2;
        return 1;
    }
}