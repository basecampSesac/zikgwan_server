package basecamp.zikgwan.ticketsale.enums;

import lombok.Getter;

@Getter
public enum Seat {

    Y("Y"),
    N("N");

    private String state;

    Seat(String state) {
        this.state = state;
    }
}
