package basecamp.zikgwan.chat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import basecamp.zikgwan.chat.enums.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChatRoomTest {

    @DisplayName("채팅방 생성 시 기본 인원수는 0이다.")
    @Test
    void defaultUserCount() {
        // given // when
        ChatRoom room = ChatRoom.builder()
                .roomName("채팅방")
                .type(RoomType.T)
                .build();

        // then
        assertThat(room.getUserCount()).isZero();
    }

    @DisplayName("사용자가 채팅방에 입장할 경우 인원수는 증가한다.")
    @Test
    void upUserCount() {
        // given
        ChatRoom room = ChatRoom.builder()
                .roomName("채팅방")
                .type(RoomType.T)
                .build();

        // when
        room.upUserCount();

        // then
        assertThat(room.getUserCount()).isEqualTo(1);
    }

    @DisplayName("사용자가 채팅방을 나갈 경우 인원수는 감소한다.")
    @Test
    void downUserCount() {
        // given
        ChatRoom room = ChatRoom.builder()
                .roomName("채팅방")
                .type(RoomType.T)
                .build();
        room.upUserCount();

        // when
        room.downUserCount();

        // then
        assertThat(room.getUserCount()).isZero();

    }

    @DisplayName("채팅방 인원수가 0에서 방을 나갈 경우 예외가 발생한다.")
    @Test
    void downUserCountWithoutUser() {
        // given
        ChatRoom room = ChatRoom.builder()
                .roomName("채팅방")
                .type(RoomType.T)
                .build();

        // when //then
        assertThatThrownBy(room::downUserCount)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("채팅방에 사용자가 존재하지 않습니다.");

    }


}