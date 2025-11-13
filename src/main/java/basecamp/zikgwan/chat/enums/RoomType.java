package basecamp.zikgwan.chat.enums;

import lombok.Getter;

@Getter
public enum RoomType {
    T("T"), // 티켓
    C("C"); // 모임

    private String type;

    RoomType(String type) {
        this.type = type;
    }
}
