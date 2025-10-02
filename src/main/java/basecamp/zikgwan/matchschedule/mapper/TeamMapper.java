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

    // 코드 → 한글
    private static final Map<String, String> CODE_TO_NAME = Map.ofEntries(
            Map.entry("OB", "두산"),
            Map.entry("LT", "롯데"),
            Map.entry("SS", "삼성"),
            Map.entry("SK", "SSG"),  // SK를 2021 이후 SSG로 보여줌
            Map.entry("HH", "한화"),
            Map.entry("KT", "KT"),
            Map.entry("NC", "NC"),
            Map.entry("LG", "LG"),
            Map.entry("HT", "기아"),
            Map.entry("WO", "키움")
    );

    // DB 저장용 한글 -> 코드로
    public static String changeNameToId(String teamName, int year) {
        if (year >= 2021) {
            return TEAM_LIST_AFTER_2021.get(teamName);
        } else {
            return TEAM_LIST_BEFORE_2021.get(teamName);
        }
    }

    // 응답용 코드 -> 한글
    public static String changeIdToName(String code) {
        return CODE_TO_NAME.getOrDefault(code, code);
    }
}