package basecamp.zikgwan.community.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.community.dto.CommunityPageResponse;
import basecamp.zikgwan.community.dto.CommunityRequest;
import basecamp.zikgwan.community.dto.CommunityResponse;
import basecamp.zikgwan.community.enums.SortType;
import basecamp.zikgwan.community.service.CommunityService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 모임 등록 POST /api/communities/{userId}
     *
     * @param request 모임 등록 요청 DTO
     * @return 등록된 모임 정보
     */
    // TODO security 설정 시 userId 삭제
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<CommunityResponse>> registerCommunity(
            @PathVariable Long userId,
            @Valid @RequestBody CommunityRequest request
    ) {
        CommunityResponse response = communityService.registerCommunity(userId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    /**
     * 전체 모임 목록 조회 페이징 기본값 최신순 RECENT 모임 인원 많은 순 MOST 모임 인원 적은 순 LEAST
     *
     * @return 모임 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CommunityPageResponse>> getAllCommunities(
            @RequestParam(required = false, defaultValue = "RECENT") SortType sortType,
            Pageable pageable
    ) {
        CommunityPageResponse response = communityService.getAllCommunities(sortType, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));

    }

    /**
     * 특정 모임 상세 조회 GET /api/communities/{communityId}
     *
     * @param communityId 모임 ID
     * @return 모임 상세 정보
     */
    @GetMapping("/{communityId}")
    public ResponseEntity<ApiResponse<CommunityResponse>> getCommunityById(
            @PathVariable Long communityId
    ) {
        CommunityResponse response = communityService.getCommunityById(communityId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));

    }

    /**
     * 모임 검색 필터링 GET /api/communities/search?title=제목&team=모임구단&stadium=구장&date=경기날짜
     *
     * @param title   검색할 제목
     * @param team    검색할 구단명
     * @param stadium 검색할 구장
     * @param date    검색할 날짜
     * @return 필터링되어 검색된 모임 목록
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> searchCommunitiesByTitle(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String stadium,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        List<CommunityResponse> response = communityService.searchCommunitiesByTitleAndTeamAndStadiumAndDate(title,
                team, stadium, date);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));

    }

}
