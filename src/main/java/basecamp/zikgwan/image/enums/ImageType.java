package basecamp.zikgwan.image.enums;

import lombok.Getter;

@Getter
public enum ImageType {
    T("T", "ticket"),    // 티켓
    U("U", "user"),   // 프로필 이미지
    C("C", "community"); // 모임

    private final String code;
    private final String path;

    ImageType(String code, String path) {
        this.code = code;
        this.path = path;
    }

    public static ImageType fromCode(String code) {
        for (ImageType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("잘못된 이미지 타입: " + code);
    }
}
