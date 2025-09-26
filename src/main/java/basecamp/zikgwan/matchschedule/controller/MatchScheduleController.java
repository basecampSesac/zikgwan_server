package basecamp.zikgwan.matchschedule.controller;

import basecamp.zikgwan.matchschedule.service.MatchScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MatchScheduleController {

    private final MatchScheduleService matchScheduleService;
}
