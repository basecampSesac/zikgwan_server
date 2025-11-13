package basecamp.zikgwan.email.repository;

import basecamp.zikgwan.email.EmailVerification;
import basecamp.zikgwan.email.enums.Verified;
import basecamp.zikgwan.email.enums.VerifiedType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<EmailVerification> findTop1ByEmailAndVerifiedTypeOrderByCreatedAtDesc(String email,
                                                                                    VerifiedType verifiedType);

    Optional<EmailVerification> findTop1ByEmailAndVerifiedTypeAndVerifiedOrderByCreatedAtDesc(
            String email, VerifiedType verifiedType, Verified verified
    );

}
