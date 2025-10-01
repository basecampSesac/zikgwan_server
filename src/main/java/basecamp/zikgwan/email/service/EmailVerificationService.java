package basecamp.zikgwan.email.service;


import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.email.EmailVerification;
import basecamp.zikgwan.email.enums.Verified;
import basecamp.zikgwan.email.repository.EmailVerificationRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationRepository verifyRepository;
    private final JavaMailSender mailSender;

    // 인증코드 발송
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        EmailVerification verify=  EmailVerification.builder()
                .email(email)
                .code(code)
                .expirationTime(expirationTime)
                .verified(Verified.N)
                .saveState(SaveState.Y)
                .build();

        //인증요청내역 저장
        verifyRepository.save(verify);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[직관] 이메일 인증 코드");
        message.setText("\n\n 직관 서비스를 이용해 주셔서 감사합니다. \n 인증코드: " + code);
        mailSender.send(message);
    }

    // 인증코드 확인
    public boolean verifyCode(String email, String code) {

        //인증만료시간비교를 위한 현재시간
        LocalDateTime verifyTime=LocalDateTime.now();

        //이메일로 인증요청 건이 있는지  확인
        EmailVerification verify = verifyRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증 요청이 없습니다."));

        System.out.println("입력코드 : " +code);
        System.out.println("DB코드 : " + verify.getCode());

        //인증요청과 코드가 맞는지 확인
        if(!verify.getCode().equals(code)){
            throw  new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }
        //인증시간 만료된 코드인지 확인
        if(!verify.getExpirationTime().isAfter(verifyTime)) {
            throw new IllegalArgumentException("인증 시간이 만료된 코드입니다.");
        }

        if (verify.getVerified() == Verified.Y) return true;

        verify.updateVerified(); // 상태 변경
        verifyRepository.save(verify);
        return true;
    }

    // 이메일 인증 여부 확인
    public boolean isVerified(String email) {
        return verifyRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .map(v -> v.getVerified() == Verified.Y)
                .orElse(false);
    }
}
