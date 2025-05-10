package com.mtech.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity

public class SecuriityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web)->web.ignoring()
                .requestMatchers("/swagger-ui/**","/v3/api-docs*/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .authorizeHttpRequests(
                        authorizeRequests ->
                                authorizeRequests.requestMatchers(HttpMethod.GET, "/api/photos/**").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/api/restaurants/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/restaurants/**").hasAuthority("SCOPE_admin") // Only admin can create restaurants
                                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasAuthority("SCOPE_admin") // Only admin can delete reviews
                                .anyRequest().authenticated()  // Other endpoints require authentication
                )
                .oauth2ResourceServer(
                        oauth2ResourceServer ->
                                oauth2ResourceServer.jwt(jwtConfigurer ->
                                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())
                                ))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf.disable());


        return http.build();

    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtScopeGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }

     class JwtScopeGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<String> scopes = jwt.getClaimAsStringList("scope");
            if (scopes == null) {
                return Collections.emptyList();
            }

            return scopes.stream()
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(Collectors.toList());
        }
    }
}
