package basecamp.zikgwan.ticketsale.enums;

import lombok.Getter;

@Getter
public enum SortType {

    RECENT("최신순"),
    HIGH("가격 높은 순"),
    LOW("가격 적은 순");

    private String type;

    SortType(String type) {
        this.type = type;
    }
}
