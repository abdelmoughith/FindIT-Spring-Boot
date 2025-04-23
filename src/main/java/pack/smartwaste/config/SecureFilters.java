package pack.smartwaste.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pack.smartwaste.models.user.User;
import pack.smartwaste.services.CustomUserService;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class SecureFilters extends OncePerRequestFilter {

    private final CustomUserService customUserService;
    private final JwtUtils jwtUtils;

    public SecureFilters(CustomUserService customUserService, JwtUtils jwtUtils) {
        this.customUserService = customUserService;

        this.jwtUtils = jwtUtils;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log incoming Content-Type
        //System.out.println("SecureFilters - Received Content-Type: " + request.getContentType());

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtUtils.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = customUserService.loadUserByUsername(username);

            if (jwtUtils.validateToken(token, user.getUsername())) {
                List<String> roleNames = jwtUtils.extractRoles(token);
                Set<SimpleGrantedAuthority> authorities = roleNames.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Extract additional fields
                Long userId = jwtUtils.extractUserId(token);
                String email = jwtUtils.extractEmail(token);

                /*
                System.out.println("Authenticated User ID: " + userId);
                System.out.println("Authenticated Email: " + email);
                System.out.println("User Roles: " + roleNames);
                 */

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
