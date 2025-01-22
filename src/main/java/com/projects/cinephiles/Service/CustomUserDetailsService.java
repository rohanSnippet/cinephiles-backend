package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;
import static org.springframework.security.core.userdetails.User.withUsername;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepository;

    public CustomUserDetailsService(UserRepo userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserBuilder builder = withUsername(user.getUsername());
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
        builder.authorities(authority);

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // This means the user is an OAuth2 user, provide a dummy password
            builder.password("DUMMY_PASSWORD");
        } else {

            builder.password(user.getPassword());
        }

        return builder.build();
    }
}

