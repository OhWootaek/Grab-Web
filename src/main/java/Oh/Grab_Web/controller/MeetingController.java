package oh.grab_web.controller;

import lombok.RequiredArgsConstructor;
import oh.grab_web.dto.ServiceDtos.MeetingCreateRequest;
import oh.grab_web.dto.ServiceDtos.MeetingResponse;
import oh.grab_web.service.MeetingService;
import oh.grab_web.service.VoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final VoteService voteService;

    // 메인 페이지
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
        }
        return "index";
    }

    // 일정 생성 페이지로 이동
    @GetMapping("/meetings/new")
    public String createMeetingForm() {
        return "meeting/create";
    }

    // 일정 생성 처리 (Form POST)
    @PostMapping("/meetings")
    public String createMeeting(@ModelAttribute MeetingCreateRequest request,
                                @AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        String meetingCode = meetingService.createMeeting(email, request);
        return "redirect:/meetings/" + meetingCode; // 생성 후 상세 페이지로 리다이렉트
    }

    // 일정 상세(투표) 페이지
    @GetMapping("/meetings/{meetingCode}")
    public String meetingDetail(@PathVariable("meetingCode") String meetingCode,
                                @AuthenticationPrincipal OAuth2User principal,
                                Model model) {
        // 1. 미팅 정보 조회
        MeetingResponse meeting = meetingService.getMeetingInfo(meetingCode);

        // 2. 방 입장 처리 (참여자로 등록)
        if (principal != null) {
            String email = principal.getAttribute("email");
            voteService.enterMeeting(email, meetingCode);
            model.addAttribute("userEmail", email);
        }

        model.addAttribute("meeting", meeting);
        return "meeting/detail";
    }
}