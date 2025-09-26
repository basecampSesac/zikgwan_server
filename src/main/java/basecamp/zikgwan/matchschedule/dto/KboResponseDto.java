package basecamp.zikgwan.matchschedule.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KboResponseDto {
    private List<ScoreboardDto> scoreboard;
    private List<DayDto> day;
}

@Data
class ScoreboardDto {
    private String idx;
    private String team;
    private int result;
    private Object i_1;
    private Object i_2;
    private Object i_3;
    private Object i_4;
    private Object i_5;
    private Object i_6;
    private Object i_7;
    private Object i_8;
    private Object i_9;
    private Object i_10;
    private Object i_11;
    private Object i_12;
    private Object i_13;
    private Object i_14;
    private Object i_15;
    private Object i_16;
    private Object i_17;
    private Object i_18;
    private int r;
    private int h;
    private int e;
    private int b;
    private int year;
    private int month;
    private int day;
    private String home;
    private String away;
    private int dbheader;
    private String place;
    private int audience;
    private String starttime;
    private String endtime;
    private String gametime;
}

@Data
class DayDto {
    private String status;
    private String date;
    private String home;
    private String away;
    private int dbheader;
    private String gameid;
}