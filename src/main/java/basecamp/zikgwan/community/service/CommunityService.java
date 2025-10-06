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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final MatchScheduleRepository matchScheduleRepository;

    // 모임 등록
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

    // 전체 모임 목록 조회
    public ApiResponse<List<CommunityResponse>> getAllCommunities() {
        List<Community> communities = communityRepository.findAll();
        List<CommunityResponse> responses = communities.stream()
            .map(CommunityResponse::from)
            .collect(Collectors.toList());
        
        return ApiResponse.success("모임 목록을 성공적으로 조회했습니다.", responses);
    }

    // 특정 모임 상세 조회
    public ApiResponse<CommunityResponse> getCommunityById(Long communityId) {
        Community community = communityRepository.findById(communityId)
            .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다. ID: " + communityId));
        
        CommunityResponse response = CommunityResponse.from(community);
        return ApiResponse.success("모임 정보를 성공적으로 조회했습니다.", response);
    }

    // 제목으로 모임 검색
    public ApiResponse<List<CommunityResponse>> searchCommunitiesByTitle(String title) {
        List<Community> communities;
        
        if (title == null || title.trim().isEmpty()) {
            // 제목이 없으면 전체 조회
            communities = communityRepository.findAll();
        } else {
            // 제목으로 검색 (대소문자 구분 없이)
            communities = communityRepository.findByTitleContainingIgnoreCase(title.trim());
        }
        
        List<CommunityResponse> responses = communities.stream()
            .map(CommunityResponse::from)
            .collect(Collectors.toList());
        
        return ApiResponse.success("검색 결과를 성공적으로 조회했습니다.", responses);
    }

    // 경기장으로 모임 검색
    public ApiResponse<List<CommunityResponse>> searchCommunitiesByStadium(String stadium) {
        List<Community> communities;
        
        if (stadium == null || stadium.trim().isEmpty()) {
            communities = communityRepository.findAll();
        } else {
            communities = communityRepository.findByStadiumContainingIgnoreCase(stadium.trim());
        }
        
        List<CommunityResponse> responses = communities.stream()
            .map(CommunityResponse::from)
            .collect(Collectors.toList());
        
        return ApiResponse.success("경기장 검색 결과를 성공적으로 조회했습니다.", responses);
    }

    // 팀으로 모임 검색
    public ApiResponse<List<CommunityResponse>> searchCommunitiesByTeam(String team) {
        List<Community> communities;
        
        if (team == null || team.trim().isEmpty()) {
            communities = communityRepository.findAll();
        } else {
            communities = communityRepository.findByTeamContainingIgnoreCase(team.trim());
        }
        
        List<CommunityResponse> responses = communities.stream()
            .map(CommunityResponse::from)
            .collect(Collectors.toList());
        
        return ApiResponse.success("팀 검색 결과를 성공적으로 조회했습니다.", responses);
    }

    // 날짜로 모임 검색
    public ApiResponse<List<CommunityResponse>> searchCommunitiesByDate(LocalDate date) {
        List<Community> communities;
        
        if (date == null) {
            communities = communityRepository.findAll();
        } else {
            communities = communityRepository.findByMatchDate(date);
        }
        
        List<CommunityResponse> responses = communities.stream()
            .map(CommunityResponse::from)
            .collect(Collectors.toList());
        
        return ApiResponse.success("해당 날짜의 모임을 성공적으로 조회했습니다.", responses);
    }

    // 모임장 닉네임으로 모임 검색
    public ApiResponse<List<CommunityResponse>> searchCommunitiesByLeader(String nickname) {
        List<Community> communities;
        
        if (nickname == null || nickname.trim().isEmpty()) {
            communities = communityRepository.findAll();
        } else {
            communities = communityRepository.findByLeaderNicknameContainingIgnoreCase(nickname.trim());
        }
        
        List<CommunityResponse> responses = communities.stream()
            .map(CommunityResponse::from)
            .collect(Collectors.toList());
        
        return ApiResponse.success("모임장 검색 결과를 성공적으로 조회했습니다.", responses);
    }
}
