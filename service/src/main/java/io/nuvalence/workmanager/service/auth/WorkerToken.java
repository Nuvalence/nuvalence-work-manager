package io.nuvalence.workmanager.service.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Worker Token is a custom Spring Security in memory authentication token used to authorize resource access.
 * -- This can be retrieved with SecurityContextHolder.getContext().getAuthentication();
 */
public class WorkerToken extends AbstractAuthenticationToken {


    private final Collection<GrantedAuthority> authorities;
    private final String principleUid;
    private final String userEmail;
    private final String originalToken;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     * @param principleUid user's UID for security context.
     * @param userEmail user's email.
     * @param originalToken token passed in authorization header.
     */
    public WorkerToken(Collection<? extends GrantedAuthority> authorities,
                       String principleUid,
                       String userEmail,
                       String originalToken) {
        super(authorities);
        this.authorities = java.util.List.copyOf(authorities);
        this.principleUid = principleUid;
        this.userEmail = userEmail;
        this.originalToken = originalToken;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.authorities;
    }

    @Override
    public Object getPrincipal() {
        return this.principleUid;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public String getOriginalToken() {
        return this.originalToken;
    }
}
