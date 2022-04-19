package ai.smartfac.logever.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getServletPath().equals("/login")) {
            filterChain.doFilter(request,response);
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if(authorizationHeader!=null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    String token = authorizationHeader.substring("Bearer ".length());
                    Algorithm algo = Algorithm.HMAC256("secret");
                    JWTVerifier jwtVerifier = JWT.require(algo).build();
                    DecodedJWT decodedJWT = jwtVerifier.verify(token);
                    String username = decodedJWT.getSubject();
                    Claim claimRoles = decodedJWT.getClaim("role");
                    Claim claimAuthorities = decodedJWT.getClaim("authority");

                    String [] roles = {};
                    String [] auths = {};
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    if(claimRoles != null) {
                        roles = claimRoles.asArray(String.class);
                        stream(roles).forEach(role->{
                            authorities.add(new SimpleGrantedAuthority(role));
                        });
                    }

                    if(claimAuthorities != null) {
                        auths = claimAuthorities.asArray(String.class);
                        stream(auths).forEach(auth->{
                            authorities.add(new SimpleGrantedAuthority(auth));
                        });
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,null,authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request,response);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.out.println("Error logging in :"+exception.getMessage());

                    Map<String,String> error = new HashMap<>();
                    error.put("error",exception.getMessage());
                    response.setContentType(APPLICATION_JSON_VALUE);
                    response.setStatus(FORBIDDEN.value());
                    new ObjectMapper().writeValue(response.getOutputStream(),error);
                }
            } else {
                filterChain.doFilter(request,response);
            }
        }
    }
}
