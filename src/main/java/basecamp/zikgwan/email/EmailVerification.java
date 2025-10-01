package basecamp.zikgwan.email;

import basecamp.zikgwan.common.domain.CreatedEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.email.enums.Verified;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "email_verification")
public class EmailVerification extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ev_id")
    private Long evId;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "code", length = 6, nullable = false)
    private String code;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'N'")
    @Column(name = "verified", nullable = false)
    private Verified verified;  // 이메일 인증 여부 (완료-Y,미완료-N)

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Y'")
    @Column(name = "save_state", nullable = false)
    private SaveState saveState;

    @Builder
    public EmailVerification(String email, String code, LocalDateTime expirationTime, Verified verified, SaveState saveState){
        this.email= email;
        this.code=code;
        this.expirationTime=expirationTime;
        this.verified = verified;
        this.saveState=saveState;
    }

    // 인증완료시 상태업데이트
    public void updateVerified(){
        this.verified = Verified.Y;
    }

}
