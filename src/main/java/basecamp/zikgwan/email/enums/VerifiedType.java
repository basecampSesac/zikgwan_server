package basecamp.zikgwan.email.enums;

import lombok.Getter;

@Getter
public enum VerifiedType {
    S("S"), // 회원가입
    P("P"); // 비밀번호재설정

    private final String state;

    VerifiedType(String state) {
        this.state = state;
    }
}
