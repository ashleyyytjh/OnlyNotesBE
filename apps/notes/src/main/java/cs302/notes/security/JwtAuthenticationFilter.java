package cs302.notes.security;

import cs302.notes.exceptions.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequest httpRequest = request;

        Cookie accessTokenCookie = WebUtils.getCookie(httpRequest, "access_token");
        Cookie idTokenCookie = WebUtils.getCookie(httpRequest, "id_token");
        if (accessTokenCookie == null || idTokenCookie == null) { throw new UnauthorizedException(); }

        String token = accessTokenCookie.getValue();
        String id_token = idTokenCookie.getValue();

        if (token != null && validateToken(token) && validateToken(id_token)) {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                // Set authentication in the context
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(claims);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                httpRequest.setAttribute("id", JwkUtil.getValueFromTokenPayload(id_token, "sub"));
                httpRequest.setAttribute("username", JwkUtil.getValueFromTokenPayload(id_token, "cognito:username"));
                httpRequest.setAttribute("email", JwkUtil.getValueFromTokenPayload(id_token, "email"));
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean validateToken(String token) {
        try {
            String kid = JwkUtil.getKidFromTokenHeader(token);
            RSAPublicKey publicKey = JwkUtil.getPublicKey(kid);
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.info(e.toString());
            logger.info("VALIDATING TOKEN FALSE");
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        try {
            String kid = JwkUtil.getKidFromTokenHeader(token);
            RSAPublicKey publicKey = JwkUtil.getPublicKey(kid);
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
