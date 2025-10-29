package org.loamok.jobs.util;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 *
 * @author Huby Franck
 */
@Component
public class ClientSignatureBuilder implements ClientSignatureUtil {
    @Override
    public String buildClientSignature(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String acceptLang = request.getHeader("Accept-Language");
        String secChUa = request.getHeader("Sec-CH-UA");
        String secChUaPlatform = request.getHeader("Sec-CH-UA-Platform");
        String screenInfo = request.getHeader("X-Screen-Info");
        String timezone = request.getHeader("X-Timezone");

        String rawSignature = String.join("|",
                userAgent != null ? userAgent : "",
                acceptLang != null ? acceptLang : "",
                secChUa != null ? secChUa : "",
                secChUaPlatform != null ? secChUaPlatform : "",
                screenInfo != null ? screenInfo : "",
                timezone != null ? timezone : ""
        );

        return buildHashedSignature(rawSignature);
    }
    
    @Override
    public String buildHashedSignature(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hash", e);
        }
        
    }
}
