package basecamp.zikgwan.matchschedule.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.matchschedule.dto.KboRequestDto;
import basecamp.zikgwan.matchschedule.dto.KboResponseDto;
import basecamp.zikgwan.matchschedule.service.MatchScheduleService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/match")
public class MatchScheduleController {

    private final MatchScheduleService matchScheduleService;

    /**
     * 매일 오전 12시 5분에 경기 일정 DB에 저장
     */
    @Scheduled(cron = "0 5 0 * * *")
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<String>> saveSchedule() {

        LocalDate today = LocalDate.now();

        List<KboResponseDto> responseDtos = matchScheduleService.saveScheduleRange(today);

        log.info("저장된 경기 일정 : {}", responseDtos);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("경기 일정 저장 완료"));
    }


    /**
     * 해당 날짜의 경기 일정 DB에서 조회
     */
    @PostMapping("/")
    public ResponseEntity<ApiResponse<List<KboResponseDto>>> getSchedule(@RequestBody KboRequestDto kboRequestDto) {

        List<KboResponseDto> responseDtos = matchScheduleService.getSchedule(kboRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(responseDtos));
    }


}
