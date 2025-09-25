package basecamp.zikgwan.review.enums;

import lombok.Getter;

@Getter
public enum Rating {
    N("N"), // 비매너
    G("G"); // 매너 좋음

    private final String rate;

    Rating(String rate) {
        this.rate = rate;
    }
}
