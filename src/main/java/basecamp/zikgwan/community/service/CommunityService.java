package basecamp.zikgwan.community.service;

import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.community.dto.CommunityRequest;
import basecamp.zikgwan.community.dto.CommunityResponse;
import basecamp.zikgwan.community.repository.CommunityRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    // 모임 등록
    @Transactional
    public CommunityResponse registerCommunity(Long userId, CommunityRequest request) {
        // 1. 모임장(User) 조회
        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("모임장을 찾을 수 없습니다. ID: " + userId));

        // 2. Community 엔티티 생성
        Community community = Community.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .stadium(request.getStadium())
                .home(request.getHome())
                .away(request.getAway())
                .memberCount(request.getMemberCount())
                .user(leader)
                .build();

        // 3. Community 저장
        Community savedCommunity = communityRepository.save(community);

        // 4. 응답 DTO 생성
        return CommunityResponse.from(savedCommunity);
    }

    // 전체 모임 목록 조회
    public List<CommunityResponse> getAllCommunities() {
        List<Community> communities = communityRepository.findAll();
        return communities.stream()
                .map(CommunityResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 모임 상세 조회
    public CommunityResponse getCommunityById(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NoSuchElementException("모임을 찾을 수 없습니다. ID: " + communityId));

        return CommunityResponse.from(community);
    }

    // 제목, 모임 구단, 구장, 경기 날짜를 선택 입력으로 필터링하여 조회
    public List<CommunityResponse> searchCommunitiesByTitleAndTeamAndStadiumAndDate(String title, String team,
                                                                                    String stadium,
                                                                                    LocalDate date) {
        LocalDateTime datetime = date.atStartOfDay();

        List<Community> communities = communityRepository.searchCommunitiesByTitleAndTeamAndStadiumAndDate(title, team,
                stadium, datetime, datetime.plusDays(1));

        return communities.stream()
                .map(c -> CommunityResponse.from(c))
                .collect(Collectors.toList());
    }

//    // 제목으로 모임 검색
//    public List<CommunityResponse> searchCommunitiesByTitle(String title) {
//        List<Community> communities;
//
//        communities = communityRepository.findByTitleContainingIgnoreCase(title.trim());
//
//        return communities.stream()
//                .map(CommunityResponse::from)
//                .collect(Collectors.toList());
//
//    }
//
//    // 경기장으로 모임 검색
//    public List<CommunityResponse> searchCommunitiesByStadium(String stadium) {
//        List<Community> communities;
//
//        if (stadium == null || stadium.trim().isEmpty()) {
//            communities = communityRepository.findAll();
//        } else {
//            communities = communityRepository.findByStadiumContainingIgnoreCase(stadium.trim());
//        }
//
//        List<CommunityResponse> responses = communities.stream()
//                .map(CommunityResponse::from)
//                .collect(Collectors.toList());
//
//        return ApiResponse.success("경기장 검색 결과를 성공적으로 조회했습니다.", responses);
//    }
//
//    // 팀으로 모임 검색
//    public ApiResponse<List<CommunityResponse>> searchCommunitiesByTeam(String team) {
//        List<Community> communities;
//
//        if (team == null || team.trim().isEmpty()) {
//            communities = communityRepository.findAll();
//        } else {
//            communities = communityRepository.findByTeamContainingIgnoreCase(team.trim());
//        }
//
//        List<CommunityResponse> responses = communities.stream()
//                .map(CommunityResponse::from)
//                .collect(Collectors.toList());
//
//        return ApiResponse.success("팀 검색 결과를 성공적으로 조회했습니다.", responses);
//    }
//
//    // 날짜로 모임 검색
//    public ApiResponse<List<CommunityResponse>> searchCommunitiesByDate(LocalDate date) {
//        List<Community> communities;
//
//        if (date == null) {
//            communities = communityRepository.findAll();
//        } else {
//            communities = communityRepository.findByMatchDate(date);
//        }
//
//        List<CommunityResponse> responses = communities.stream()
//                .map(CommunityResponse::from)
//                .collect(Collectors.toList());
//
//        return ApiResponse.success("해당 날짜의 모임을 성공적으로 조회했습니다.", responses);
//    }
//
//    // 모임장 닉네임으로 모임 검색
//    public ApiResponse<List<CommunityResponse>> searchCommunitiesByLeader(String nickname) {
//        List<Community> communities;
//
//        if (nickname == null || nickname.trim().isEmpty()) {
//            communities = communityRepository.findAll();
//        } else {
//            communities = communityRepository.findByLeaderNicknameContainingIgnoreCase(nickname.trim());
//        }
//
//        List<CommunityResponse> responses = communities.stream()
//                .map(CommunityResponse::from)
//                .collect(Collectors.toList());
//
//        return ApiResponse.success("모임장 검색 결과를 성공적으로 조회했습니다.", responses);
//    }
}
