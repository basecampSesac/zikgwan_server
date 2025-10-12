package basecamp.zikgwan.community.service;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.community.dto.CommunityPageResponse;
import basecamp.zikgwan.community.dto.CommunityRequest;
import basecamp.zikgwan.community.dto.CommunityResponse;
import basecamp.zikgwan.community.enums.SortType;
import basecamp.zikgwan.community.repository.CommunityRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public CommunityPageResponse getAllCommunities(SortType sortType, Pageable pageable) {

        Page<Community> communities = checkSortBy(sortType, pageable);

        List<CommunityResponse> content = communities
                .map(CommunityResponse::from)
                .getContent();

        return CommunityPageResponse.builder()
                .content(content)
                .page(communities.getNumber())
                .size(communities.getSize())
                .totalElements(communities.getTotalElements())
                .totalPages(communities.getTotalPages())
                .last(communities.isLast())
                .build();
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

        List<Community> communities;

        // date null 체크
        if (date != null) {
            LocalDateTime datetime = date.atStartOfDay();

            communities = communityRepository.searchCommunitiesByTitleAndTeamAndStadiumAndDate(title,
                    team,
                    stadium, datetime, datetime.plusDays(1));
        } else {
            communities = communityRepository.searchCommunitiesByTitleAndTeamAndStadiumAndDate(title,
                    team,
                    stadium, null, null);
        }

        return communities.stream()
                .map(c -> CommunityResponse.from(c))
                .collect(Collectors.toList());
    }

    // 정렬 조건 확인 및 정렬
    private Page<Community> checkSortBy(SortType sortType, Pageable pageable) {
        Page<Community> communities;

        // 최신순
        if (sortType == null || sortType.equals(SortType.RECENT)) {
            communities = communityRepository.findAllBySaveStateOrderByCreatedAtDesc(SaveState.Y, pageable);

            // 인원 많은 순
        } else if (sortType.equals(SortType.MOST)) {
            communities = communityRepository.findAllBySaveStateOrderByMemberCountDesc(SaveState.Y, pageable);

            // 인원 적은 순
        } else {
            communities = communityRepository.findAllBySaveStateOrderByMemberCountAsc(SaveState.Y, pageable);
        }
        return communities;
    }
}
