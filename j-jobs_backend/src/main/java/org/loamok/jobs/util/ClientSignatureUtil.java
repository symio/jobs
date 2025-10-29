package org.loamok.jobs.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Huby Franck
 */
public interface ClientSignatureUtil {
    String buildClientSignature(HttpServletRequest request);
    String buildHashedSignature(String source);
}
