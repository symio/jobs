package org.loamok.jobs.dto.email.constants;

import lombok.NoArgsConstructor;

/**
 *
 * @author Huby Franck
 */
@NoArgsConstructor
public final class EmailMessageConstants {
    public static final String EMAIL_ADMIN_SUFFIX = "_ADMIN";
    public static final String EMAIL_MESSAGE_SUBSTITUTION_SEPARATOR = "%%%";
    public static final String EMAIL_MESSAGE_STARTING = """
        Bonjour %%%U_NAME%%% %%%U_FIRSTNAME%%%,

        """;
    public static final String EMAIL_MESSAGE_ENDING = 
        """
        Cordialement,

        --,
        L'\u00e9quipe de Jobs
        %%%BASE_URL%%%/mentions-legales 
        """;
    
}
