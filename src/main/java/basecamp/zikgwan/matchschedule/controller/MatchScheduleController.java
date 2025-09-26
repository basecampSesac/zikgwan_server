package basecamp.zikgwan.matchschedule.controller;

import basecamp.zikgwan.matchschedule.dto.KboRequestDto;
import basecamp.zikgwan.matchschedule.dto.KboResponseDto;
import basecamp.zikgwan.matchschedule.service.MatchScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/match")
public class MatchScheduleController {

    private final MatchScheduleService matchScheduleService;

    @PostMapping("/schedule")
    public ResponseEntity<KboResponseDto> getSchedule(@RequestBody KboRequestDto kboRequestDto) {
        KboResponseDto response = matchScheduleService.getSchedule(kboRequestDto);

        return ResponseEntity.ok().body(response);
    }

}
