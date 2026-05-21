package smartparkingsystem.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import smartparkingsystem.backend.service.auth.TokenRedisService;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenRedisService tokenRedisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                if (tokenProvider.isRefreshToken(jwt)) {
                    log.warn("Refresh token used as access token");
                    filterChain.doFilter(request, response);
                    return;
                }

                if (tokenRedisService.isAccessTokenBlacklisted(jwt)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String userId = tokenProvider.getUserIdFromToken(jwt);
                UUID userIdUUID = UUID.fromString(userId);

                // Check if user has been deleted
                if (tokenRedisService.isUserDeleted(userIdUUID)) {
                    log.warn("Token used for deleted user. UserId: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                try {
                    UserDetails userDetails = userDetailsService.loadUserById(userIdUUID);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (UsernameNotFoundException ex) {
                    log.warn("User not found or has been deleted. UserId: {}, Exception: {}", userId, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
