package cs302.notes.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import cs302.notes.security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final List<String> allowedOrigins = new ArrayList<>() {
        { add("https://staging.onlynotes.net");}
        { add("https://www.onlynotes.net"); }
        { add("http://localhost:5173"); }
        { add("https://apis.onlynotes.net"); }
    };

    /*
     * Setting up of Cross-Origin Resource Sharing
     *
     * Defines a CORS filter that allows requests from a specific origin
     * (http://localhost:3000)
     * and specifies the HTTP methods and headers that are allowed.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // Create a new CorsConfiguration object, which will hold the CORS configuration
        // settings
        CorsConfiguration configuration = new CorsConfiguration();
        // Specifies the allowed origins (i.e., domains) that are permitted to make
        // cross-origin requests to your server
        configuration.setAllowedOrigins(allowedOrigins);
        // Specify the HTTP methods that are allowed for cross-origin requests, which
        // accepts all methods here
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        // Specify the headers that are allowed for cross-origin requests, which accepts
        // all here
        configuration.setAllowedHeaders(List.of("*"));
        // Allows the inclusion of credentials (e.g., cookies) in cross-origin requests
        configuration.setAllowCredentials(true);
        // Creates a source for CORS configuration
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Applies CORS configuration to all paths on your server
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors().and() // Enable CORS
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher(HttpMethod.GET, "/health")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/notes")).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterAfter(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

}
