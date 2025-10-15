package basecamp.zikgwan.community.dto;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.community.enums.CommunityState;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {

    private Long communityId;
    private String title;
    private String description;
    private LocalDateTime date;
    private Integer memberCount;
    private String stadium;
    private String home;
    private String away;
    private String nickname;
    private CommunityState state;
    private SaveState saveState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //imageUrl 있는 경우
    private String imageUrl;

    public static CommunityResponse from(Community community) {
        return from(community, null); // 기본적으로 imageUrl 없음
    }


    public static CommunityResponse from(Community community, String imageUrl) {
        return CommunityResponse.builder()
                .communityId(community.getCommunityId())
                .title(community.getTitle())
                .description(community.getDescription())
                .date(community.getDate())
                .stadium(community.getStadium())
                .home(community.getHome())
                .away(community.getAway())
                .memberCount(community.getMemberCount())
                .nickname(community.getUser().getNickname())
                .state(community.getState())
                .saveState(community.getSaveState())
                .createdAt(community.getCreatedAt())
                .updatedAt(community.getUpdatedAt())
                .imageUrl(imageUrl)
                .build();
    }
}
