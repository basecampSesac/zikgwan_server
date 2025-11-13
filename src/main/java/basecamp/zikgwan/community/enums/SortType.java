package basecamp.zikgwan.community.enums;

import lombok.Getter;

@Getter
public enum SortType {

    RECENT("최신순"),
    MOST("인원 많은 순"),
    LEAST("인원 적은 순");

    private String type;

    SortType(String type) {
        this.type = type;
    }
}
