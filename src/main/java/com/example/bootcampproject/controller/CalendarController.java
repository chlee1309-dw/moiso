package com.example.bootcampproject.controller;

import com.example.bootcampproject.config.NoLoginRequired;
import com.example.bootcampproject.dto.request.CalendarEventCreateRequest;
import com.example.bootcampproject.dto.response.AppointmentResponse;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.entity.CalendarEventEntity;
import com.example.bootcampproject.entity.enums.AppointmentStatus;
import com.example.bootcampproject.service.AppointmentService;
import com.example.bootcampproject.service.CalendarEventService;
import com.example.bootcampproject.service.NotificationService;
import com.example.bootcampproject.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** 내 예약 + 직접 추가한 일정을 모아 보여주는 캘린더 화면과 휴대폰 캘린더 구독용 ICS(webcal) 피드. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/calendars")
public class CalendarController {

    private static final DateTimeFormatter ICS_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter ICS_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ICS_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final AppointmentService appointmentService;
    private final CalendarEventService calendarEventService;
    private final UserService userService;
    private final NotificationService notificationService;

    // 내 캘린더 화면 (예약 + 직접 추가한 일정을 날짜별로 묶어서 표시)
    @GetMapping
    public String calendar(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                           HttpServletRequest request,
                           Model model) {
        List<EventItem> items = myEvents(loginUser.idx());
        model.addAttribute("events", groupByDate(items));

        String token = userService.getOrCreateCalendarToken(loginUser.idx());
        model.addAttribute("calendarToken", token);
        model.addAttribute("feedUrl", baseUrl(request) + "/calendars/feed/" + token);

        model.addAttribute("recentNotifications", notificationService.findRecent(loginUser.idx()));
        model.addAttribute("unreadNotifCount", notificationService.countUnread(loginUser.idx()));
        return "Calendar";
    }

    // 개인 일정 직접 추가
    @PostMapping("/events")
    public String addEvent(@Valid @ModelAttribute CalendarEventCreateRequest request,
                           BindingResult bindingResult,
                           @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "날짜와 제목을 입력해 주세요.");
            return "redirect:/calendars";
        }
        calendarEventService.create(loginUser.idx(), request);
        redirectAttributes.addFlashAttribute("message", "일정을 추가했어요.");
        return "redirect:/calendars?date=" + request.date();
    }

    // 개인 일정 삭제 (본인 것만)
    @PostMapping("/events/{idx}/delete")
    public String deleteEvent(@PathVariable Long idx,
                              @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                              RedirectAttributes redirectAttributes) {
        calendarEventService.delete(idx, loginUser.idx());
        redirectAttributes.addFlashAttribute("message", "일정을 삭제했어요.");
        return "redirect:/calendars";
    }

    // 휴대폰 캘린더 구독용 ICS 피드. 토큰 자체가 인증 수단이라 로그인 불필요(webcal 구독은 헤더를 못 보냄).
    @NoLoginRequired
    @GetMapping("/feed/{token}")
    @ResponseBody
    public ResponseEntity<byte[]> feed(@PathVariable String token) {
        Long userIdx = userService.getUserIdxByCalendarToken(token);
        byte[] body = buildIcs(myEvents(userIdx)).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=UTF-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"moiso-calendar.ics\"")
                .body(body);
    }

    // 멘티/멘토로 잡힌 예약(거절·취소 제외) + 직접 추가한 일정을 합쳐서 날짜순 정렬
    private List<EventItem> myEvents(Long userIdx) {
        Stream<EventItem> fromAppointments = Stream.concat(
                        appointmentService.findAsMentee(userIdx).stream(),
                        appointmentService.findAsMentor(userIdx).stream())
                .filter(a -> a.status() != AppointmentStatus.REJECTED && a.status() != AppointmentStatus.CANCELLED)
                .map(a -> new EventItem(null, a.date(), a.time(), eventTitle(a, userIdx), a.locationName(), false));

        Stream<EventItem> fromPersonal = calendarEventService.findMine(userIdx).stream()
                .map(e -> new EventItem(e.getIdx(), e.getDate(), e.getTime(), e.getTitle(), null, true));

        return Stream.concat(fromAppointments, fromPersonal)
                .sorted(Comparator.comparing(EventItem::date)
                        .thenComparing(EventItem::time, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
    }

    private Map<String, List<CalendarEvent>> groupByDate(List<EventItem> items) {
        Map<String, List<CalendarEvent>> events = new LinkedHashMap<>();
        for (EventItem item : items) {
            String timeLabel = item.time() != null ? item.time().toString() : "종일";
            events.computeIfAbsent(item.date().toString(), k -> new ArrayList<>())
                    .add(new CalendarEvent(item.id(), timeLabel, item.title(), item.removable()));
        }
        return events;
    }

    private String eventTitle(AppointmentResponse a, Long viewerIdx) {
        boolean viewerIsMentee = a.menteeIdx().equals(viewerIdx);
        String counterpart = viewerIsMentee ? a.mentorName() : a.menteeName();
        String counterpartRole = viewerIsMentee ? "멘토" : "멘티";
        String title = counterpart + " " + counterpartRole + "와 멘토링";
        return a.status() == AppointmentStatus.REQUESTED ? title + " (대기중)" : title;
    }

    private String buildIcs(List<EventItem> items) {
        String stamp = LocalDateTime.now().format(ICS_STAMP);
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n")
          .append("VERSION:2.0\r\n")
          .append("PRODID:-//MoISO//Mentoring Calendar//KO\r\n")
          .append("CALSCALE:GREGORIAN\r\n")
          .append("METHOD:PUBLISH\r\n");

        for (EventItem item : items) {
            sb.append("BEGIN:VEVENT\r\n")
              .append("UID:").append(item.removable() ? "event-" : "appointment-")
              .append(item.id() != null ? item.id() : item.date() + "-" + item.title().hashCode())
              .append("@moiso\r\n")
              .append("DTSTAMP:").append(stamp).append("\r\n");

            if (item.time() != null) {
                LocalDateTime start = LocalDateTime.of(item.date(), item.time());
                sb.append("DTSTART:").append(start.format(ICS_DATE_TIME)).append("\r\n")
                  .append("DTEND:").append(start.plusHours(1).format(ICS_DATE_TIME)).append("\r\n");
            } else {
                sb.append("DTSTART;VALUE=DATE:").append(item.date().format(ICS_DATE)).append("\r\n")
                  .append("DTEND;VALUE=DATE:").append(item.date().plusDays(1).format(ICS_DATE)).append("\r\n");
            }

            sb.append("SUMMARY:").append(escapeIcs(item.title())).append("\r\n");
            if (item.location() != null) {
                sb.append("LOCATION:").append(escapeIcs(item.location())).append("\r\n");
            }
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escapeIcs(String value) {
        return value.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;");
    }

    private String baseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    // 예약이든 직접 추가한 일정이든 화면/ICS 생성에 공통으로 쓰는 내부 표현
    private record EventItem(Long id, LocalDate date, LocalTime time, String title, String location, boolean removable) {}

    private record CalendarEvent(Long id, String time, String title, boolean removable) {}
}
