package basecamp.zikgwan.community.service;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.community.dto.CommunityRequest;
import basecamp.zikgwan.community.dto.CommunityResponse;
import basecamp.zikgwan.community.repository.CommunityRepository;
import basecamp.zikgwan.matchschedule.MatchSchedule;
import basecamp.zikgwan.matchschedule.repository.MatchScheduleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final MatchScheduleRepository matchScheduleRepository;

    /**
     * 모임 등록
     */
    @Transactional
    public ApiResponse<CommunityResponse> registerCommunity(Long leaderId, CommunityRequest request) {
        // 1. 모임장(User) 조회
        User leader = userRepository.findById(leaderId)
            .orElseThrow(() -> new IllegalArgumentException("모임장을 찾을 수 없습니다. ID: " + leaderId));

        // 2. 경기 일정(MatchSchedule) 조회
        MatchSchedule matchSchedule = matchScheduleRepository.findById(request.getScheduleId())
            .orElseThrow(() -> new IllegalArgumentException("경기 일정을 찾을 수 없습니다. ID: " + request.getScheduleId()));

        // 3. Community 엔티티 생성
        Community community = Community.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .matchSchedule(matchSchedule) // ERD에 따라 MatchSchedule 엔티티 직접 연결
            .memberCount(request.getMemberCount())
            .leader(leader) // ERD에 따라 leader 필드 직접 연결
            .build();

        // 4. Community 저장
        Community savedCommunity = communityRepository.save(community);

        // 5. 응답 DTO 생성
        CommunityResponse response = CommunityResponse.from(savedCommunity);

        return ApiResponse.success("모임이 성공적으로 등록되었습니다.", response);
    }
}
