package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.config.JwtConfig;
import br.com.ecomercial.partnership.dto.AuthRequest;
import br.com.ecomercial.partnership.dto.AuthResponse;
import br.com.ecomercial.partnership.entity.User;
import br.com.ecomercial.partnership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
    
    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        // Simple password validation (in production, use proper password encoder)
        if (!"admin123".equals(request.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String token = jwtConfig.generateToken(user);
        
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .partnerId(user.getPartnerId())
                .build();
    }
}
