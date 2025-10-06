package basecamp.zikgwan.community.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.community.dto.CommunityRequest;
import basecamp.zikgwan.community.dto.CommunityResponse;
import basecamp.zikgwan.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
