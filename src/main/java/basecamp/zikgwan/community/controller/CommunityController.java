package basecamp.zikgwan.community.controller;

import basecamp.zikgwan.common.aop.LoginCheck;
import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.community.dto.CommunityPageResponse;
import basecamp.zikgwan.community.dto.CommunityRequest;
import basecamp.zikgwan.community.dto.CommunityResponse;
import basecamp.zikgwan.community.enums.CommunityState;
import basecamp.zikgwan.community.enums.SortType;
import basecamp.zikgwan.community.service.CommunityService;
import basecamp.zikgwan.config.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 모임 등록 POST /api/communities/{userId}
     *
     * @param request
     * @param imageFile
     * @param principal
     * @return
     * @throws Exception
     */
    @LoginCheck
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CommunityResponse>> registerCommunity(
            @Valid @RequestPart("data") CommunityRequest request,
            @RequestPart("image") MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) throws Exception {

        //System.out.println("getTitle " + request.getTitle());
        CommunityResponse response = communityService.registerCommunity(principal.getUserId(), request, imageFile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    /**
     * 모임 수정 PUT /api/SocialLogin/{communityId}
     *
     * @param communityId
     * @param request
     * @param imageFile
     * @param principal
     * @return
     * @throws Exception
     */
    @LoginCheck
    @PutMapping(value = "/{communityId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CommunityResponse>> updateCommunity(
            @PathVariable Long communityId,
            @Valid @RequestPart("data") CommunityRequest request,
            @RequestPart("image") MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) throws Exception {

        Long userId = principal.getUserId();
        CommunityResponse response = communityService.updateCommunity(userId, communityId, request, imageFile);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    /**
     * 모임 삭제 (soft delete)
     *
     * @param communityId
     * @param principal
     * @return
     */
    @LoginCheck
    @DeleteMapping("/{communityId}")
    public ResponseEntity<ApiResponse<String>> deleteCommunity(
            @PathVariable Long communityId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        communityService.deleteCommunity(communityId, principal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("모임이 삭제되었습니다."));
    }

    /**
     * 모임 상태 변경 (ING > END)
     *
     * @param communityId
     * @param principal
     * @return
     */
    @LoginCheck
    @PutMapping("/state/{communityId}")
    public ResponseEntity<ApiResponse<String>> toggleCommunityState(
            @PathVariable Long communityId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        CommunityState newState = communityService.updateCommunityState(communityId, principal.getUserId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("모임 상태가 " + newState.getState() + "로 변경되었습니다."));
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

    /**
     * 모집 마감 임박 모임 4개 조회
     *
     * @return 모임 4개
     */
    @GetMapping("/closing-soon")
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> getClosingSoonCommunities() {
        List<CommunityResponse> responses = communityService.getClosingSoonCommunities();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(responses));
    }

}
