package cl.duoc.donaton.msdonaciones.config;

import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final String ROLES_HEADER = "X-User-Roles";
    private static final String USER_HEADER  = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info(">>> GatewayAuthFilter ejecutándose, X-User-Roles: {}",
            request.getHeader("X-User-Roles"));
        String rolesHeader = request.getHeader(ROLES_HEADER);

        if (StringUtils.hasText(rolesHeader)) {
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            String principal = StringUtils.hasText(request.getHeader(USER_HEADER))
                    ? request.getHeader(USER_HEADER)
                    : "gateway-user";

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
