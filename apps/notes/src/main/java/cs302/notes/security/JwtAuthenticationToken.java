package cs302.notes.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import io.jsonwebtoken.Claims;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Claims claims;

    public JwtAuthenticationToken(Claims claims) {
        super(null);
        this.claims = claims;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return claims.getSubject();
    }

}
