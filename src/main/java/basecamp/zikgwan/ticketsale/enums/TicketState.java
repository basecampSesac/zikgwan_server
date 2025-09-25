package basecamp.zikgwan.ticketsale.enums;

import lombok.Getter;

@Getter
public enum TicketState {

    ING("I"),
    END("E");

    private String state;

    TicketState(String state) {
        this.state = state;
    }
}
