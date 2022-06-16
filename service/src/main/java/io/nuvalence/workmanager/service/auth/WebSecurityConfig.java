package io.nuvalence.workmanager.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Customized WebSecurityConfigurationAdaptor for handling authorization.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();
        http.cors().disable();
        http.authorizeRequests().anyRequest().permitAll(); // open all paths

        /* Custom configuration reliant on firebase auth implementation
        // in house csrf protection is not required with token authorization and stateless session.
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Open routes, swagger definition, and admin for dummy data script.
        // TODO: remove admin route from permit all once we have a better way to provide testing data.
        http.authorizeRequests().antMatchers("/swagger-ui.html", "/admin/**").permitAll();
        http.authorizeRequests().anyRequest().authenticated();  // Requires non ant-matched paths to be authed.
        http.addFilterBefore(new TokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.cors().configurationSource(corsConfigurationSource()); // configure cors
         */
    }

    /**
     * This opens up swagger definition file resources.
     * @param web WebSecurity client.
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/webjars/springfox-swagger-ui/**");
        web.ignoring().antMatchers("/swagger-ui.html");
        web.ignoring().antMatchers("/swagger-resources/**");
        web.ignoring().antMatchers("/v2/**");
        web.ignoring().antMatchers("/");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://dummy-url.com",
                "https://dummy-url-2.com",
                "http://localhost:4200", "http://localhost:4201"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("accept", "authorization", "content-type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
