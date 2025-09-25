package basecamp.zikgwan.review.enums;

import lombok.Getter;

@Getter
public enum RefType {
    T("T"),    // 티켓
    G("G"); // 모임

    private final String type;

    RefType(String type) {
        this.type = type;
    }
}
