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

    Optional<EmailVerification> findFirstByEmailAndVerifiedTypeOrderByCreatedAtDesc(String email,
                                                                                    VerifiedType verifiedType);

    // 이메일 + verifiedType 조건 + verified = Y 인 가장 최근 데이터 조회
    @Query("SELECT e.createdAt " +
            "FROM EmailVerification e " +
            "WHERE e.email = :email " +
            "AND e.verifiedType = :verifiedType " +
            "AND e.verified = :verified " +
            "ORDER BY e.createdAt DESC")
    LocalDateTime findLatestCreatedAtByEmailAndVerifiedTypeAndVerified(
            @Param("email") String email,
            @Param("verifiedType") VerifiedType verifiedType,
            @Param("verified") Verified verified
    );

}
