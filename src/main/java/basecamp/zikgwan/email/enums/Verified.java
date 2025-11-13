package basecamp.zikgwan.email.enums;

import lombok.Getter;

@Getter
public enum Verified {

    N("N"), // 인증 안 됨
    Y("Y"); // 인증 성공

    private final String state;

    Verified(String state) {
        this.state = state;
    }
}
