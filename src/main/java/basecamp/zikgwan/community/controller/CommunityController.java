package basecamp.zikgwan.community.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.community.dto.CommunityRequest;
import basecamp.zikgwan.community.dto.CommunityResponse;
import basecamp.zikgwan.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 모임 등록
     * POST /api/communities
     * @param leaderId 모임장 ID (Header: X-User-Id)
     * @param request 모임 등록 요청 DTO
     * @return 등록된 모임 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommunityResponse>> registerCommunity(
        @RequestHeader("X-User-Id") Long leaderId,
        @Valid @RequestBody CommunityRequest request
    ) {
        ApiResponse<CommunityResponse> response = communityService.registerCommunity(leaderId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 전체 모임 목록 조회
     * GET /api/communities
     * @return 모임 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> getAllCommunities() {
        ApiResponse<List<CommunityResponse>> response = communityService.getAllCommunities();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 특정 모임 상세 조회
     * GET /api/communities/{communityId}
     * @param communityId 모임 ID
     * @return 모임 상세 정보
     */
    @GetMapping("/{communityId}")
    public ResponseEntity<ApiResponse<CommunityResponse>> getCommunityById(
        @PathVariable Long communityId
    ) {
        ApiResponse<CommunityResponse> response = communityService.getCommunityById(communityId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 모임 검색 (제목으로 검색)
     * GET /api/communities/search?title=검색어
     * @param title 검색할 제목
     * @return 검색된 모임 목록
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> searchCommunitiesByTitle(
        @RequestParam(required = false) String title
    ) {
        ApiResponse<List<CommunityResponse>> response = communityService.searchCommunitiesByTitle(title);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 경기장으로 모임 검색
     * GET /api/communities/search/stadium?stadium=경기장명
     * @param stadium 검색할 경기장명
     * @return 검색된 모임 목록
     */
    @GetMapping("/search/stadium")
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> searchCommunitiesByStadium(
        @RequestParam(required = false) String stadium
    ) {
        ApiResponse<List<CommunityResponse>> response = communityService.searchCommunitiesByStadium(stadium);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 팀으로 모임 검색
     * GET /api/communities/search/team?team=팀명
     * @param team 검색할 팀명
     * @return 검색된 모임 목록
     */
    @GetMapping("/search/team")
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> searchCommunitiesByTeam(
        @RequestParam(required = false) String team
    ) {
        ApiResponse<List<CommunityResponse>> response = communityService.searchCommunitiesByTeam(team);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 날짜로 모임 검색
     * GET /api/communities/search/date?date=2024-05-15
     * @param date 검색할 날짜
     * @return 검색된 모임 목록
     */
    @GetMapping("/search/date")
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> searchCommunitiesByDate(
        @RequestParam(required = false) LocalDate date
    ) {
        ApiResponse<List<CommunityResponse>> response = communityService.searchCommunitiesByDate(date);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 모임장으로 모임 검색
     * GET /api/communities/search/leader?nickname=모임장닉네임
     * @param nickname 검색할 모임장 닉네임
     * @return 검색된 모임 목록
     */
    @GetMapping("/search/leader")
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> searchCommunitiesByLeader(
        @RequestParam(required = false) String nickname
    ) {
        ApiResponse<List<CommunityResponse>> response = communityService.searchCommunitiesByLeader(nickname);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
