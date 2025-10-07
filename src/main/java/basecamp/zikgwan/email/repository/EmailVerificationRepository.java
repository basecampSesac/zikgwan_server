package basecamp.zikgwan.email.repository;

import basecamp.zikgwan.email.EmailVerification;
import basecamp.zikgwan.email.enums.VerifiedType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<EmailVerification> findFirstByEmailAndVerifiedTypeOrderByCreatedAtDesc(String email,
                                                                                    VerifiedType verifiedType);
}
