package pack.smartwaste.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.catalina.filters.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pack.smartwaste.Utils.UrlUtils;
import pack.smartwaste.services.CustomUserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class Config {


    private final SecureFilters secureFilters;
    private final PostAuthorizationFilter postAuthFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(UrlUtils.REACT_URL, UrlUtils.REACT_URL_LOCALHOST, "http://localhost:8080")); // Use arrays for better compatibility
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        //config.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity https) throws Exception {
        https
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/graphiql/**", "/graphql/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/data/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/annonces/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/annonces/**").authenticated()
                                .requestMatchers(HttpMethod.PUT, "/annonces/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/annonces/**").authenticated()
                                .requestMatchers("/user/**").authenticated()
                                .requestMatchers("/ws/**").permitAll()
                                .anyRequest().permitAll()
                )

                //.httpBasic(Customizer.withDefaults())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(postAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(secureFilters, UsernamePasswordAuthenticationFilter.class)
        ;
        return https.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public UserDetailsService userDetailsService(CustomUserService customUserService) {
        return customUserService;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserService customUserService
    ) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(customUserService);
        return new ProviderManager(authProvider);
    }


    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            System.out.println("AuthenticationEntryPoint triggered for: " + request.getRequestURI());

            // Log request details
            /*
            System.out.println("Method: " + request.getMethod());
            System.out.println("Headers: ");

             */
            Collections.list(request.getHeaderNames()).forEach(header ->
                    System.out.println(header + ": " + request.getHeader(header))
            );

            // Log query parameters
            //System.out.println("Query Parameters: " + request.getQueryString());

            // Read and log request body (only works for small requests)
            String requestBody = getRequestBody(request);
            //System.out.println("Request Body: " + requestBody);

            // Send response
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getOutputStream().println("{ \"error\": \"Forbidden: Unauthorized Request\" }");
        };
    }

    // Helper method to read request body
    private String getRequestBody(HttpServletRequest request) {
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestBody.toString();
    }



}
