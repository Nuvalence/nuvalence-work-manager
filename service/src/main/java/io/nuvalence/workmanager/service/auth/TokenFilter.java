package io.nuvalence.workmanager.service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Custom JWT filter to verify Google Identity access token for routing access.
 */
@Slf4j
public class TokenFilter extends OncePerRequestFilter {

    /**
     * This filter is a part of Spring Security's filter chain that occurs on every request.
     * @param request http request object.
     * @param response http response object.
     * @param filterChain the main filter present in spring security, is made of multiple sections.
     * @throws ServletException thrown if there is any server communication issues.
     * @throws IOException thrown if there are any IO issues exist in the method.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // If we are targeting the swagger document, ignore the jwt filter.
        // TODO: Configure setup-demo-data alternative so that we can pass in an access token and protect admin route.
        if (request.getServletPath().equals("/swagger-ui.html")
                || request.getServletPath().equals("/admin/**")) {
            filterChain.doFilter(request, response);
        } else {
            // Pull Bearer token from request.
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                // If the token exists, we want to try and verify it and inform the server.
                try {
                    String token = authorizationHeader.substring("Bearer ".length());

                    // Verify token to firebase, build a security token if valid.
                    WorkerToken workerToken = buildTokenForSecurityContext(token);

                    /*
                    Security context is an in memory repository that can store details.
                    If the token is verified via the Google Identity parameters then we can
                    authorize a token for the security context holder and apply it.
                    * */
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(workerToken);
                    }

                    // process request.
                    filterChain.doFilter(request, response);
                } catch (Exception e) {
                    log.error("There has been an issue with the provided jwt, error: {}", e.getMessage());
                    if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        SecurityContextHolder.getContext().setAuthentication(null);
                    }
                    TokenSecurityExceptionWrapper exception = new TokenSecurityExceptionWrapper(e.getMessage(),
                            HttpStatus.FORBIDDEN, ZonedDateTime.now());
                    response.setStatus(exception.getHttpStatus().value());
                    response.getWriter().write(convertObjectToJson(exception));

                }
            } else {
                filterChain.doFilter(request, response);
            }

        }
    }

    /**
     * Verifies the JWT provided is valid by Identity Platform and creates a worker token if valid.
     * @param token String JWT token.
     * @return A new worker token.
     * @throws FirebaseAuthException if token exception.
     */
    private WorkerToken buildTokenForSecurityContext(String token) throws FirebaseAuthException {
        try {
            // Verifies jwt token and created a decoded token.
            FirebaseToken authToken = FirebaseAuth.getInstance().verifyIdToken(token);

            /*
            In this case we are adding a generic authority role_user, we are not checking anything by this currently,
            however, we could build this off of the decoded token when available IE: tenantId and then set that as
            a condition to match in the web security config through an ant matcher.
            */
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            return new WorkerToken(authorities, authToken.getUid(), authToken.getEmail(), token);

        } catch (FirebaseAuthException authException) {
            log.error(authException.getMessage());
            throw authException;
        }

    }

    /**
     * Simple Mapper for response exception.
     * @param object input object.
     * @return Object in JSON String.
     * @throws JsonProcessingException if there is a processing issue.
     */
    private String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

}
