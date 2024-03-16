package ai.smartfac.logever.filter;

import ai.smartfac.logever.entity.AuditTrail;
import ai.smartfac.logever.entity.User;
import ai.smartfac.logever.service.AuditTrailService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private String sessionTimeout;

    private String sessionTimeoutAlert;

    public AuditTrailService auditTrailService;

    private AuthenticationManager authenticationManager;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, String sessionTimeout, String sessionTimeoutAlert) {
        this.authenticationManager = authenticationManager;
        this.sessionTimeout = sessionTimeout;
        this.sessionTimeoutAlert = sessionTimeoutAlert;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username,password);
        if(auditTrailService==null){
            ServletContext servletContext = request.getServletContext();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            auditTrailService = webApplicationContext.getBean(AuditTrailService.class);
        }
        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String accessToken="";
        String refreshToken="";
        if(authResult.getPrincipal() instanceof  LdapUserDetailsImpl){
            LdapUserDetailsImpl user = (LdapUserDetailsImpl)authResult.getPrincipal();
            Algorithm algo = Algorithm.HMAC256("secret");
            accessToken = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis()+10*60*1000))
                    .withIssuer(request.getRequestURL().toString())
                    .withClaim("role",user.getAuthorities().stream().filter(f->f.getAuthority().startsWith("ROLE_")).map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .withClaim("authority",user.getAuthorities().stream().filter(f->!f.getAuthority().startsWith("ROLE_")).map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
//                .withClaim("firstName",user.getFirst_name())
//                .withClaim("lastName",user.getLast_name())
//                .withClaim("department",user.getDepartment().getName())
//                .withClaim("reporting_manager",user.getReporting_manager())
//                .withClaim("email",user.getEmail())
//                .withClaim("employee_code",user.getEmployee_code())
//                .withClaim("user_id",user.getId())
//                .withClaim("windows_id",user.getWindows_id())
//                .withClaim("designation",user.getDesignation())
//                .withClaim("hire_date",user.getHireDate().toString())
//                .withClaim("site",user.getDepartment().getSite())
//                .withClaim("fullName",user.getFullName())
                    .withClaim("sessionTimeout",sessionTimeout)
                    .withClaim("sessionTimeoutAlert",sessionTimeoutAlert)
                    .sign(algo);
            refreshToken = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis()+10*60*1000))
                    .withIssuer(request.getRequestURL().toString())
                    .sign(algo);
        } else{
            User user = (User)authResult.getPrincipal();
            Algorithm algo = Algorithm.HMAC256("secret");
             accessToken = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis()+10*60*1000))
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
                    .withClaim("sessionTimeout",sessionTimeout)
                    .withClaim("sessionTimeoutAlert",sessionTimeoutAlert)
                    .sign(algo);

            refreshToken = JWT.create()
                    .withSubject(user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis()+10*60*1000))
                    .withIssuer(request.getRequestURL().toString())
                    .sign(algo);

        }


        Map<String,String> tokens = new HashMap<>();
        tokens.put("access_token",accessToken);
        tokens.put("refresh_token",refreshToken);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(),tokens);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401);
        Map<String,String> error = new HashMap<>();
        error.put("error","Username or password is invalid!");
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(),error);
        auditTrailService.save(new AuditTrail("USER","UNKNOWN","UNAUTHENTICATED ACCESS","","Failed login attempt",request.getParameter("username")));
    }
}
