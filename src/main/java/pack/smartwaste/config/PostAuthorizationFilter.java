package pack.smartwaste.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pack.smartwaste.models.post.Annonce;
import pack.smartwaste.rep.AnnonceRepository;

import java.io.IOException;

@Component
public class PostAuthorizationFilter extends OncePerRequestFilter {

    private final AnnonceRepository annonceRepository;
    private final JwtUtils jwtUtils;

    public PostAuthorizationFilter(AnnonceRepository annonceRepository, JwtUtils jwtUtils) {
        this.annonceRepository = annonceRepository;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        // Log incoming Content-Type
        //System.out.println("PostAuthorizationFilter - Received Content-Type: " + request.getContentType());


        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtUtils.extractUsername(token);
        Long userId = jwtUtils.extractUserId(token);

        if (request.getMethod().equals("PUT") || request.getMethod().equals("DELETE")) {
            Long postId = extractPostIdFromRequest(request);

            if (postId != null) {
                Annonce annonce = annonceRepository.findById(postId).orElse(null);

                if (annonce == null || !annonce.getUser().getId().equals(userId)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("You are not authorized to modify this post.");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private Long extractPostIdFromRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String[] parts = path.split("/");
        try {
            return Long.parseLong(parts[parts.length - 1]); // Assuming post ID is the last segment in URL
        } catch (NumberFormatException e) {
            return null;
        }
    }


}

