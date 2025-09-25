package basecamp.zikgwan.community.enums;

import lombok.Getter;

@Getter
public enum CommunityState {
    ING("I"),
    END("E");

    private String state;

    CommunityState(String state) {
        this.state = state;
    }
}
