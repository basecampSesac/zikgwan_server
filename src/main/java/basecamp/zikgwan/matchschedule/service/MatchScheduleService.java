package basecamp.zikgwan.matchschedule.service;

import basecamp.zikgwan.matchschedule.dto.KboRequestDto;
import basecamp.zikgwan.matchschedule.dto.KboResponseDto;
import basecamp.zikgwan.matchschedule.repository.MatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchScheduleService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final WebClient webClient;

    public KboResponseDto getSchedule(KboRequestDto kboRequestDto) {

        KboResponseDto kboResponseDto = webClient.post()
                .uri("/")
                .body(Mono.just(kboRequestDto), KboRequestDto.class)
                .retrieve()
                .bodyToMono(KboResponseDto.class)
                .block();

        return kboResponseDto;
    }
}
