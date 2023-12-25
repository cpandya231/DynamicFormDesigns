package ai.smartfac.logever.controller;

import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class LoginController {

    @Autowired
    UserService userService;

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if(authorizationHeader!=null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refreshToken = authorizationHeader.substring("Bearer ".length());
                Algorithm algo = Algorithm.HMAC256("secret");
                JWTVerifier jwtVerifier = JWT.require(algo).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(refreshToken);
                String username = decodedJWT.getSubject();
                User user = userService.getUserByUsername(username).get();
                String accessToken = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis()+60*60*1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("role",user.getAuthorities().stream().filter(f->f.getAuthority().startsWith("ROLE_")).map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                        .withClaim("authority",user.getAuthorities().stream().filter(f->!f.getAuthority().startsWith("ROLE_")).map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                        .withClaim("firstName",user.getFirst_name())
                        .withClaim("lastName",user.getLast_name())
                        .withClaim("department",user.getDepartment().getName())
                        .withClaim("reporting_manager",user.getReporting_manager())
                        .withClaim("email",user.getEmail())
                        .withClaim("employee_code",user.getEmployee_code())
                        .withClaim("user_id",user.getId())
                        .withClaim("windows_id",user.getWindows_id())
                        .withClaim("designation",user.getDesignation())
                        .withClaim("hire_date",user.getHireDate().toString())
                        .withClaim("site",user.getDepartment().getSite())
                        .withClaim("fullName",user.getFullName())
                        .sign(algo);

                Map<String,String> tokens = new HashMap<>();
                tokens.put("access_token",accessToken);
                tokens.put("refresh_token",refreshToken);
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(),tokens);
            } catch (Exception exception) {
                exception.printStackTrace();
                System.out.println("Error logging in :" + exception.getMessage());

                Map<String, String> error = new HashMap<>();
                error.put("error", exception.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                response.setStatus(FORBIDDEN.value());
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }
}
