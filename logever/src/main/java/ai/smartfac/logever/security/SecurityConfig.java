package ai.smartfac.logever.security;

import ai.smartfac.logever.filter.CustomAuthenticationFilter;
import ai.smartfac.logever.filter.CustomAuthorizationFilter;
import ai.smartfac.logever.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;


@Order(1)
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${app.session.timeout}")
    private String sessionTimeout;

    @Value("${app.session.timeout.alert}")
    private String sessionTimeoutAlert;
    @Value("${spring.ldap.urls:180.190.51.100}")
    private String adIpAddress;

    @Value("${spring.ldap.username:username}")
    private String ldapUserName;
    @Value("${spring.ldap.base:dc=example,dc=com}")
    private String ldapBase;

    @Value("${spring.ldap.password:password}")
    private String ldapPassword;

    @Value("${ldap.uid.attribute:uid}")
    private String uidAttribute;
    @Value("${ldap.password.attribute:password}")
    private String passwordAttribute;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    AppUserDetailsService appUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean(), sessionTimeout, sessionTimeoutAlert);
        //customAuthenticationFilter.setFilterProcessesUrl("/login");
        http.cors().configurationSource(request -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(List.of("*"));
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            cors.setAllowedHeaders(List.of("*"));
            return cors;
        }).and().csrf().disable();
//        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers("/static/css/**", "/static/js/**");
        http.authorizeRequests().antMatchers("/login", "/token/refresh").permitAll();
        http.authorizeRequests().antMatchers("/master/entry/bulk/template/**").permitAll();
        http.authorizeRequests().antMatchers("/logout").permitAll();
        http.authorizeRequests().antMatchers("/users/register").permitAll();
        http.authorizeRequests().antMatchers("/users/**").permitAll();
//                .and().formLogin().loginPage("/index.html");
        http.httpBasic().and().authorizeRequests().antMatchers("/").permitAll().and().csrf().disable();
//        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(appUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
        auth
                .ldapAuthentication()
                .userDnPatterns(String.format("%s={0}",uidAttribute))
                .contextSource()
                .url(String.format("%s/%s", adIpAddress, ldapBase))
                .managerDn(ldapUserName) // Bind user DN
                .managerPassword(ldapPassword)
                .and()
                .passwordCompare()
                .passwordEncoder(passwordEncoder())
                .passwordAttribute(passwordAttribute);
    }

    private PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
