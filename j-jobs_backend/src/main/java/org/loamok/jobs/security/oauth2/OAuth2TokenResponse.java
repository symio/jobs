package org.loamok.jobs.security.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Huby Franck
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("remember_me_token")
    private String rememberMeToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("expires_in")
    private Integer expiresIn;
    
    @JsonProperty("originally_expires")
    private Long originallyExpires;
    
    private String scope;
}
