package cl.duoc.donaton.msdonaciones.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayAuthFilter gatewayAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado"))
            )
            .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Error dispatch interno de Tomcat (no debe bloquear con 401)
                .requestMatchers("/error").permitAll()
                // Swagger / OpenAPI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                // Endpoints públicos
                .requestMatchers(HttpMethod.GET, "/api/donaciones/top").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/donaciones/transparencia").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/donaciones/ultimas").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/donaciones/total").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/donaciones/count").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/causas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/testimonios").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/imagenes/uploads/**").permitAll()
                // Endpoints protegidos (usuario autenticado)
                .requestMatchers(HttpMethod.GET, "/api/donaciones/mis-donaciones").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/donaciones").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/donaciones/**").hasAnyRole("ADMIN", "CENTRO_ADMIN")
                // Endpoints protegidos (ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/donaciones").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/donaciones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/causas").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/causas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/causas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/centros/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/centros").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/centros/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/centros/**").hasAnyRole("ADMIN", "CENTRO_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/centros/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/donaciones/centro/**").hasAnyRole("ADMIN", "CENTRO_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/necesidades/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/centros/*/necesidades").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/centros/*/necesidades").hasAnyRole("ADMIN", "CENTRO_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/necesidades/**").hasAnyRole("ADMIN", "CENTRO_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/necesidades/**").hasAnyRole("ADMIN", "CENTRO_ADMIN")
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<GatewayAuthFilter> gatewayAuthFilterRegistration(GatewayAuthFilter filter) {
        FilterRegistrationBean<GatewayAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
