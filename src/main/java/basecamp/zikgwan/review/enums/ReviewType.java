package basecamp.zikgwan.review.enums;

import lombok.Getter;

@Getter
public enum ReviewType {
    T("T"),    // 티켓
    C("C"); // 모임

    private final String type;

    ReviewType(String type) {
        this.type = type;
    }
}
