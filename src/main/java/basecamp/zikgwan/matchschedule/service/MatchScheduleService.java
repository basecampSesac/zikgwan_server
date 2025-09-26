package basecamp.zikgwan.matchschedule.service;

import basecamp.zikgwan.matchschedule.repository.MatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchScheduleService {

    private final MatchScheduleRepository matchScheduleRepository;
}
