package com.mtech.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/swagger-ui/**", "/v3/api-docs*/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http.authorizeHttpRequests(
                        authorizeRequests -> authorizeRequests
                                .requestMatchers(HttpMethod.GET, "/api/photos/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/restaurants/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/restaurants/*/reviews")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/restaurants/*/reviews")
                                .hasAuthority("SCOPE_restaurant-user") // Only user can create reviews
                                .requestMatchers(HttpMethod.PUT, "/api/restaurants/*/reviews")
                                .hasAuthority("SCOPE_restaurant-user") // Only user can update reviews
                                .requestMatchers(HttpMethod.POST, "/api/restaurants/**")
                                .hasAuthority("SCOPE_restaurant-admin") // Only admin can create restaurants
                                .requestMatchers(HttpMethod.DELETE, "/api/restaurants/**")
                                .hasAuthority("SCOPE_restaurant-admin") // Only admin can create restaurants
                                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**")
                                .hasAuthority("SCOPE_restaurant-admin") // Only admin can delete reviews
                                .anyRequest()
                                .authenticated() // Other endpoints require authentication
                )
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(
                        jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new java.util.ArrayList<>();

            // Extract realm roles if needed
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null && resourceAccess.containsKey("restaurant-review-app")) {
                Map<String, Object> appAccess = (Map<String, Object>) resourceAccess.get("restaurant-review-app");
                if (appAccess.containsKey("roles")) {
                    List<String> roles = (List<String>) appAccess.get("roles");
                    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + role)));
                }
            }

            // Also extract roles from "scope" claim (if needed)
            Object scopeClaim = jwt.getClaim("scope");
            if (scopeClaim instanceof String scopeStr) {
                for (String scope : scopeStr.split(" ")) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                }
            }


            return authorities;
        });

        return jwtAuthenticationConverter;
    }


    // Custom converter to map the scope claim to authorities
    /*class JwtScopeGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Get the "scope" claim from the JWT
            Object scopeClaim = jwt.getClaim("scope");
            System.out.println("Scope Claim: " + scopeClaim); // Print the scope claim
            if (scopeClaim instanceof String scopeStr) {
                // Split the scopes by space and convert them into GrantedAuthorities
                return List.of(scopeStr.split(" ")).stream()
                        .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                        .collect(Collectors.toList());
            } else if (scopeClaim instanceof Collection<?> scopeList) {
                // If it's a collection of scopes, map them to GrantedAuthorities
                return ((Collection<?>) scopeList)
                        .stream()
                        .filter(String.class::isInstance)
                        .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList(); // Return an empty list if there are no scopes
        }
    }*/
}
