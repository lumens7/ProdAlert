package br.com.lumens.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
Criado por Luís
*/

@Component
public class EmailVerificationService {

    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class VerificationCode {
        private String code;
        private LocalDateTime expirationTime;
        private String email;
        private boolean used;
    }

    public String generateVerificationCode(String email) {
        String code = String.valueOf(100000 + random.nextInt(900000));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        verificationCodes.put(email, new VerificationCode(code, expirationTime, email, false));
        return code;
    }

    public boolean verifyCode(String email, String code) {
        VerificationCode storedCode = verificationCodes.get(email);

        if (storedCode == null || storedCode.used || LocalDateTime.now().isAfter(storedCode.expirationTime)) {
            return false;
        }

        return storedCode.code.equals(code);
    }

    public void invalidateCode(String email) {
        VerificationCode code = verificationCodes.get(email);
        if (code != null) {
            code.used = true;
        }
    }

    public void cleanExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expirationTime));
    }
}
