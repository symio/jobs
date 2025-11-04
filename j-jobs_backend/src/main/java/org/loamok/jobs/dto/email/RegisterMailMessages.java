package org.loamok.jobs.dto.email;

import org.loamok.jobs.dto.email.constants.EmailMessageConstants;
import org.loamok.jobs.dto.email.constants.RegisterEmailConstants;
import org.springframework.stereotype.Service;
import org.loamok.jobs.dto.email.interfaces.RegisterEmail;

/**
 *
 * @author Huby Franck
 */
@Service
public class RegisterMailMessages implements RegisterEmail {

    @Override
    public String getEmailMessage(String messageKey) {
        StringBuilder subResult = new StringBuilder();
        
        switch (messageKey) {
            case RegisterEmailConstants.EMAIL_MESSAGE_PREFIX +"_VALIDATION" 
                -> subResult.append(RegisterEmailConstants.REGISTER_EMAIL_VALIDATION);
            case RegisterEmailConstants.EMAIL_MESSAGE_PREFIX +"_INVALIDATION" 
                -> subResult.append(RegisterEmailConstants.REGISTER_EMAIL_INVALIDATION);
            case RegisterEmailConstants.EMAIL_MESSAGE_PREFIX +"_INVALIDATION" + EmailMessageConstants.EMAIL_ADMIN_SUFFIX 
                    -> subResult.append(RegisterEmailConstants.REGISTER_EMAIL_INVALIDATION_ADMIN);
        }
        
        return subResult.toString();
    }

    @Override
    public String getEmailTitle(String titleKey) {
        StringBuilder subResult = new StringBuilder();
        
        switch (titleKey) {
            case RegisterEmailConstants.EMAIL_MESSAGE_PREFIX +"_VALIDATION_TITLE" 
                -> subResult.append(RegisterEmailConstants.REGISTER_EMAIL_VALIDATION_TITLE);
            case RegisterEmailConstants.EMAIL_MESSAGE_PREFIX +"_INVALIDATION_TITLE" 
                -> subResult.append(RegisterEmailConstants.REGISTER_EMAIL_INVALIDATION_TITLE);
        }
        
        return subResult.toString();
    }
    
}
