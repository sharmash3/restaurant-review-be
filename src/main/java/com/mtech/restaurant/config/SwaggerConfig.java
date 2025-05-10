package com.mtech.restaurant.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@SecurityScheme(
//        name = "Keycloak",
//        openIdConnectUrl = "http://localhost:9090/realms/restaurant-review/.well-known/openid-configuration",
//        scheme = "bearer",
//        type = SecuritySchemeType.OPENIDCONNECT,
//        in = SecuritySchemeIn.HEADER
//
//)
@SecurityScheme(
        name = "Keycloak",
        type = SecuritySchemeType.OAUTH2,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "http://localhost:9090/realms/restaurant-review/protocol/openid-connect/auth",
                        tokenUrl = "http://localhost:9090/realms/restaurant-review/protocol/openid-connect/token",
                        scopes = {
                                @OAuthScope(name = "openid", description = "OpenID scope"),
                                @OAuthScope(name = "profile", description = "User profile info"),
                                @OAuthScope(name = "admin", description = "Admin permissions for restaurant creation and review deletion"),
                                @OAuthScope(name = "user", description = "User permissions for viewing reviews")
                        }
                )
        )
)
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi restaurantApi() {
        return GroupedOpenApi.builder()
                .group("restaurant")
                .pathsToMatch("/**")
                .build();
    }
}
