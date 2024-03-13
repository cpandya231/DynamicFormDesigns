package ai.smartfac.logever.security;

import ai.smartfac.logever.filter.CustomAuthenticationFilter;
import ai.smartfac.logever.filter.CustomAuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Order(1)
@Configuration
@EnableWebSecurity
public class LdapSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${ad.server.ip.address:180.190.51.100}")
    private String adIpAddress;

    @Value("${spring.ldap.embedded.port:389}")
    private String adPort;
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .ldapAuthentication()
                .userDnPatterns("uid={0},ou=people")
                .groupSearchBase("ou=groups")
                .contextSource()
                    .url(String.format("ldap://%s:%s/dc=springframework,dc=org",adIpAddress,adPort))
                    .and()
                .passwordCompare()
                    .passwordEncoder(new BCryptPasswordEncoder())
                    .passwordAttribute("userPassword");
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());
//        //customAuthenticationFilter.setFilterProcessesUrl("/login");
//        System.out.println("Authenticating Vihit");
//        http.cors().configurationSource(request -> {
//            var cors = new CorsConfiguration();
//            cors.setAllowedOrigins(List.of("*"));
//            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//            cors.setAllowedHeaders(List.of("*"));
//            return cors;
//        }).and().csrf().disable();
//        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        http.authorizeRequests().antMatchers("/login", "/token/refresh").permitAll();
//        http.authorizeRequests().antMatchers("/master/entry/bulk/template/**").permitAll();
//        http.authorizeRequests().antMatchers("/logout").permitAll();
//        http.authorizeRequests().antMatchers("/users/register").permitAll();
//        http.authorizeRequests().antMatchers("/users/**").permitAll()
//                .and().formLogin();
//        http.authorizeRequests().anyRequest().authenticated();
//    }

}
