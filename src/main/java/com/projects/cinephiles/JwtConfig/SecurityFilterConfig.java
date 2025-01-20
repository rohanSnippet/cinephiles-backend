package com.projects.cinephiles.JwtConfig;

import com.projects.cinephiles.Config.CustomOAuth2SuccessHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@AllArgsConstructor
@EnableMethodSecurity
public class SecurityFilterConfig {

    private JwtAuthenticationEntryPoint point;
    private JwtAuthenticationFilter filter;
    private UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {


        return security.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> corsConfiguration()))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**","/oauth2/**","/movie/**","/show/**","/theatre/get-theatres/by-location","/actor/**","/").permitAll()
                        .anyRequest().authenticated())
                //.oauth2Login(oauth2-> oauth2.loginPage("http://localhost:5173").successHandler(oAuth2SuccessHandler))
                //.oauth2Login(oauth2 -> oauth2.successHandler(oAuth2SuccessHandler))
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("http://localhost:5173/",true))
                 .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
               // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .build();

       /* return security.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> corsConfiguration()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/oauth2/**", "/movie/**", "/show/**", "/theatre/get-theatres/by-location", "/actor/**", "/").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("http://localhost:5173/", true)
                        .failureUrl("/auth/login?error=true")) // Redirect on failure
                .formLogin(form -> form
                        .loginProcessingUrl("http://localhost:5173/login") // Custom login endpoint
                        .successForwardUrl("http://localhost:5173/") // Redirect on success
                        .failureUrl("http://localhost:5173/login") // Redirect on failure
                        .permitAll()) // Allow everyone to access the login page
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(point)) // Handle unauthorized access
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // Use stateful session management
                .build();*/
    }


    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CorsConfiguration corsConfiguration() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:5173/"); // Allow this specific origin
        corsConfiguration.addAllowedMethod("*"); // Allow all methods
        corsConfiguration.addAllowedHeader("*"); // Allow all headers
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return corsConfiguration;
    }
}
