package org.loamok.jobs.dto.email;

import org.loamok.jobs.dto.email.constants.ResetChallengeConstants;
import org.loamok.jobs.dto.email.interfaces.ResetChallenge;
import org.springframework.stereotype.Service;

/**
 *
 * @author Huby Franck
 */
@Service
public class ResetChallengeMessages implements ResetChallenge {

    @Override
    public String getEmailMessage(String messageKey) {
        StringBuilder subResult = new StringBuilder();
        
        switch (messageKey) {
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_KEY_GENERATION" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_KEY_GENERATION);
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_INVALIDATION" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_INVALIDATION);
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_INVALIDATION_ADMIN" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_INVALIDATION_ADMIN);
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_VALIDATION" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_VALIDATION);
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_DISABLING" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_DISABLING);
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_DISABLING_ADMIN" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_DISABLING_ADMIN);
            default -> throw new AssertionError();
        }
        
        return subResult.toString();
    }

    @Override
    public String getEmailTitle(String titleKey) {
        StringBuilder subResult = new StringBuilder();
        
        switch (titleKey) {
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_KEY_GENERATION_TITLE" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_KEY_GENERATION_TITLE);
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_INVALIDATION_TITLE" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_INVALIDATION_TITLE);
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_VALIDATION_TITLE" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_VALIDATION_TITLE);
            case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX +"_DISABLING_TITLE" 
                -> subResult.append(ResetChallengeConstants.RESET_CHALLENGE_DISABLING_TITLE);
            default -> throw new AssertionError();
        }
        
        return subResult.toString();
    }
    
}
