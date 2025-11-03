package basecamp.zikgwan.matchschedule.service;

import basecamp.zikgwan.config.slack.SlackNotifier;
import basecamp.zikgwan.matchschedule.MatchSchedule;
import basecamp.zikgwan.matchschedule.dto.KboRequestDto;
import basecamp.zikgwan.matchschedule.dto.KboResponseDto;
import basecamp.zikgwan.matchschedule.mapper.TeamMapper;
import basecamp.zikgwan.matchschedule.repository.MatchScheduleRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchScheduleService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final RestTemplate restTemplate;
    private final SlackNotifier slackNotifier;

    @Value("${kbo.data.url}")
    private String url; // kbodate 요청용 url

    // 패턴 정의 (yyyyMMdd)
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 오늘 포함 ±7일 간의 경기 일정을 저장
    @Transactional
    @CacheEvict(value = "matchSchedule", allEntries = true) // 저장과 동시에 캐시 삭제
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 10000) // 10초 간격 재시도
    )
    public List<KboResponseDto> saveScheduleRange(LocalDate today) {

        log.info("[스케줄러 시작] {} 기준 ±7일 경기 일정 저장 시작", today);
        List<KboResponseDto> dtos = new ArrayList<>();

        for (int i = -7; i <= 7; i++) {
            LocalDate targetDate = today.plusDays(i);

            List<KboResponseDto> responseDtos = saveSchedule(
                    targetDate.getYear(),
                    targetDate.getMonthValue(),
                    targetDate.getDayOfMonth()
            );

            dtos.addAll(responseDtos);
        }

        log.info("[스케줄러 완료] {} ~ {} 저장된 경기 수: {}",
                today.minusDays(7), today.plusDays(7), dtos.size());

        return dtos;
    }

    // DB에 경기 일정 저장
    @Transactional
    public List<KboResponseDto> saveSchedule(int year, int month, int day) {

        KboRequestDto date = KboRequestDto.builder()
                .year(year)
                .month(month)
                .day(day)
                .build();

        ResponseEntity<Map<String, Object>> responseEntity =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        new HttpEntity<>(date),
                        new ParameterizedTypeReference<>() {
                        }
                );

        Map<String, Object> response = responseEntity.getBody();

        // "day" 키의 데이터만 추출
        List<Map<String, Object>> dayList = (List<Map<String, Object>>) response.get("day");

        List<KboResponseDto> responseDtos = convertToDto(dayList, response);

        saveDB(responseDtos);

        return responseDtos;
    }

    // 경기 일정 조회
//    public List<KboResponseDto> getSchedule(KboRequestDto kboRequestDto) {
//        LocalDate matchDate = LocalDate.of(kboRequestDto.getYear(), kboRequestDto.getMonth(), kboRequestDto.getDay());
//
//        List<MatchSchedule> matchSchedules = matchScheduleRepository.findAllByMatchDate(matchDate);
//
//        // 경기 일정이 없으면 빈 리스트 반환
//        if (matchSchedules.isEmpty()) {
//            return List.of(KboResponseDto.builder().build());
//        }
//
//        return matchSchedules.stream()
//                .map(m -> {
//                    return KboResponseDto.builder()
//                            .date(m.getMatchDate())
//                            .home(TeamMapper.changeIdToName(m.getHomeTeam())) // 한글 이름으로 변환
//                            .away(TeamMapper.changeIdToName(m.getAwayTeam()))
//                            .place(m.getStadium())
//                            .build();
//                }).toList();
//    }

    // 경기 일정 조회 (캐시 적용)
    @Cacheable(
            cacheNames = "matchSchedule",
            key = "#kboRequestDto.year + '-' + #kboRequestDto.month + '-' + #kboRequestDto.day"
            , unless = "#result == null || #result.isEmpty()" // 빈 데이터는 캐시하지 않음
    )
    public List<KboResponseDto> getSchedule(KboRequestDto kboRequestDto) {

        LocalDate matchDate = LocalDate.of(
                kboRequestDto.getYear(),
                kboRequestDto.getMonth(),
                kboRequestDto.getDay()
        );

        List<MatchSchedule> matchSchedules = matchScheduleRepository.findAllByMatchDate(matchDate);

        // 빈 리스트도 가능
        if (matchSchedules.isEmpty()) {
            return List.of(KboResponseDto.builder().build());
        }

        return matchSchedules.stream()
                .map(m -> KboResponseDto.builder()
                        .date(m.getMatchDate())
                        .home(TeamMapper.changeIdToName(m.getHomeTeam()))
                        .away(TeamMapper.changeIdToName(m.getAwayTeam()))
                        .place(m.getStadium())
                        .build())
                .toList();
    }

    private List<KboResponseDto> convertToDto(List<Map<String, Object>> dayList, Map<String, Object> response) {
        if (dayList == null || dayList.isEmpty()) {
            log.warn("dayList가 비어 있습니다. 응답 데이터: {}", response);
            return Collections.emptyList();
        }

        return dayList.stream()
                .map(d -> {
                    // 문자열 날짜 → LocalDate 변환
                    LocalDate localDate = LocalDate.parse((String) d.get("date"), formatter);

                    return KboResponseDto.builder()
                            .date(localDate)
                            .home((String) d.get("home"))
                            .away((String) d.get("away"))
                            .place(findPlace(response, d.get("home"), d.get("away"), localDate.getYear()))
                            .build();
                })
                .toList();
    }

    private void saveDB(List<KboResponseDto> responseDtos) {
        if (responseDtos == null || responseDtos.isEmpty()) {
            log.info(" 저장할 경기 일정이 없습니다.");
            return;
        }

        List<MatchSchedule> matchSchedules = responseDtos.stream()
                .map(r -> {
                    return matchScheduleRepository.findByMatchDateAndHomeTeamAndAwayTeam(
                            r.getDate(), r.getHome(), r.getAway()
                    ).map(m -> {
                        // 이미 있으면 업데이트
                        m.updateStadium((r.getPlace() == null || r.getPlace().isBlank()) ? "미정" : r.getPlace());
                        return m;
                    }).orElseGet(() -> {
                        // 없으면 새로 저장
                        return MatchSchedule.builder()
                                .matchDate(r.getDate())
                                .homeTeam(r.getHome())
                                .awayTeam(r.getAway())
                                .stadium((r.getPlace() == null || r.getPlace().isBlank()) ? "미정" : r.getPlace())
                                .build();
                    });
                })
                .toList();

        matchScheduleRepository.saveAll(matchSchedules);
    }

    // scoreboard에서 경기장(place) 매칭
    private String findPlace(Map<String, Object> response, Object home, Object away, int year) {
        List<Map<String, Object>> scoreboard = (List<Map<String, Object>>) response.get("scoreboard");

        if (scoreboard == null || scoreboard.isEmpty()) {
            log.warn("scoreboard 데이터가 없습니다. home={}, away={}, year={}", home, away, year);
            return "미정";
        }

        return scoreboard.stream()
                .filter(sb -> {
                    String sbHomeCode = TeamMapper.changeNameToId((String) sb.get("home"), year);
                    String sbAwayCode = TeamMapper.changeNameToId((String) sb.get("away"), year);
                    return home.equals(sbHomeCode) && away.equals(sbAwayCode);
                })
                .map(sb -> (String) sb.get("place"))
                .findFirst()
                .orElse(null);
    }

    // 모든 재시도 실패 시 실행
    @Recover
    public List<KboResponseDto> recover(Exception e, LocalDate today) {
        log.error("[최종 실패] 경기 일정 저장 3회 재시도 후 실패: {}", e.getMessage(), e);
        slackNotifier.send("""
                *[최종 실패] 경기 일정 저장 실패*
                • 날짜: %s
                • 예외: `%s`
                """.formatted(today, e.getMessage()));

        return List.of();
    }
}