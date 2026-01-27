package oh.grab_web.controller;

import lombok.RequiredArgsConstructor;
import oh.grab_web.dto.ServiceDtos.HeatMapResponse;
import oh.grab_web.dto.ServiceDtos.VoteRequest;
import oh.grab_web.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteApiController {

    private final VoteService voteService;

    // 투표 제출 (JSON)
    @PostMapping("/{meetingCode}")
    public ResponseEntity<String> submitVote(@PathVariable String meetingCode,
                                             @RequestBody VoteRequest request,
                                             @AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        voteService.submitVote(email, meetingCode, request.slots());
        return ResponseEntity.ok("Voted Successfully");
    }

    // 히트맵 데이터 조회 (JSON) -> 프론트엔드에서 렌더링
    @GetMapping("/{meetingCode}")
    public ResponseEntity<List<HeatMapResponse>> getHeatMap(@PathVariable String meetingCode) {
        List<HeatMapResponse> heatMap = voteService.getHeatMap(meetingCode);
        return ResponseEntity.ok(heatMap);
    }
}