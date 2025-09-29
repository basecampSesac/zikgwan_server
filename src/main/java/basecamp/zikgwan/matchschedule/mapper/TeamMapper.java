package basecamp.zikgwan.matchschedule.mapper;

import java.util.Map;

/**
 * 2021 전과 후의 팀 이름 매핑
 */
public class TeamMapper {
    private static final Map<String, String> TEAM_LIST_BEFORE_2021 = Map.ofEntries(
            Map.entry("KIA", "HT"),
            Map.entry("두산", "OB"),
            Map.entry("롯데", "LT"),
            Map.entry("NC", "NC"),
            Map.entry("SK", "SK"),
            Map.entry("LG", "LG"),
            Map.entry("넥센", "WO"),
            Map.entry("키움", "WO"),
            Map.entry("히어로즈", "WO"),
            Map.entry("우리", "WO"),
            Map.entry("한화", "HH"),
            Map.entry("삼성", "SS"),
            Map.entry("KT", "KT")
    );

    private static final Map<String, String> TEAM_LIST_AFTER_2021 = Map.ofEntries(
            Map.entry("KIA", "HT"),
            Map.entry("두산", "OB"),
            Map.entry("롯데", "LT"),
            Map.entry("NC", "NC"),
            Map.entry("SSG", "SK"),  // SK → SSG
            Map.entry("LG", "LG"),
            Map.entry("넥센", "WO"),
            Map.entry("키움", "WO"),
            Map.entry("히어로즈", "WO"),
            Map.entry("우리", "WO"),
            Map.entry("한화", "HH"),
            Map.entry("삼성", "SS"),
            Map.entry("KT", "KT")
    );

    public static String changeNameToId(String teamName, int year) {
        if (year >= 2021) {
            return TEAM_LIST_AFTER_2021.get(teamName);
        } else {
            return TEAM_LIST_BEFORE_2021.get(teamName);
        }
    }
}