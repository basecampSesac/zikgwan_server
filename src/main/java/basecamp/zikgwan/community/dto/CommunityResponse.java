package basecamp.zikgwan.community.dto;

import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.community.enums.CommunityState;
import basecamp.zikgwan.common.enums.SaveState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {

    private Long communityId;
    private String title;
    private String description;
    private Integer memberCount;
    private CommunityState state;
    private SaveState saveState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 경기 일정 정보
    private Long scheduleId;
    private String matchDate;
    private String homeTeam;
    private String awayTeam;
    private String stadium;

    // 모임장 정보
    private Long leaderId;
    private String leaderNickname;

    public static CommunityResponse from(Community community) {
        return CommunityResponse.builder()
                .communityId(community.getCommunityId())
                .title(community.getTitle())
                .description(community.getDescription())
                .memberCount(community.getMemberCount())
                .state(community.getState())
                .saveState(community.getSaveState())
                .createdAt(community.getCreatedAt())
                .updatedAt(community.getUpdatedAt())
                .scheduleId(community.getMatchSchedule().getScheduleId())
                .matchDate(community.getMatchSchedule().getMatchDate().toString()) // LocalDate to String
                .homeTeam(community.getMatchSchedule().getHomeTeam())
                .awayTeam(community.getMatchSchedule().getAwayTeam())
                .stadium(community.getMatchSchedule().getStadium())
                .leaderId(community.getLeader().getUserId())
                .leaderNickname(community.getLeader().getNickname())
                .build();
    }
}
