package basecamp.zikgwan.common.enums;

import lombok.Getter;

@Getter
public enum SaveState {
    Y("Y"), // 저장
    N("N"); // 삭제

    private final String state;

    SaveState(String state) {
        this.state = state;
    }
}
