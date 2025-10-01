package basecamp.zikgwan.notification.enums;

import lombok.Getter;

@Getter
public enum IsRead {

    N("N"), // 안 읽음
    Y("Y"); // 읽음

    private final String state;

    IsRead(String state) {
        this.state = state;
    }
}
