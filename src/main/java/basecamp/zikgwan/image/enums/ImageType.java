package basecamp.zikgwan.image.enums;

import lombok.Getter;

@Getter
public enum ImageType {
    T("T"),    // 티켓
    U("U"), // 프로필 이미지
    C("C"); // 모임

    private final String type;

    ImageType(String type) {
        this.type = type;
    }
}
