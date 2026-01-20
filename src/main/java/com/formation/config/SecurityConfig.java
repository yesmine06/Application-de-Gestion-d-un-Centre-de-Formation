package com.formation.config;

import com.formation.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(CustomUserDetailsService userDetailsService, 
                         CustomAuthenticationSuccessHandler authenticationSuccessHandler,
                         RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                         CorsConfigurationSource corsConfigurationSource) {
        this.userDetailsService = userDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configuration CSRF : désactivé pour les API REST (utilisent JWT), activé pour les pages web
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
                // Désactiver CSRF uniquement pour les endpoints API (ils utilisent JWT)
                .ignoringRequestMatchers("/api/**")
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            // Permettre les sessions pour les pages web (Thymeleaf), JWT pour les API REST
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Pages publiques
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/spa/**", "/react/**", "/favicon.ico").permitAll()
                // Logout doit être accessible (géré par Spring Security)
                .requestMatchers("/logout").permitAll()
                // API d'authentification publique
                .requestMatchers("/api/auth/**").permitAll()
                // Swagger/OpenAPI documentation (public en développement)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Actuator health endpoint (public pour monitoring)
                .requestMatchers("/actuator/health").permitAll()
                // Autres endpoints Actuator (authentifiés)
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                // API REST peut utiliser JWT ou session
                .requestMatchers("/api/**").authenticated()
                // Pages SSR (protégées par session)
                .requestMatchers("/admin/dashboard", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/formateur/dashboard", "/formateur/**").hasAnyRole("FORMATEUR", "ADMIN")
                .requestMatchers("/etudiant/dashboard", "/etudiant/**").hasAnyRole("ETUDIANT", "ADMIN", "FORMATEUR")
                .requestMatchers("/profile", "/profile/**").authenticated()
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(restAuthenticationEntryPoint)
            )
            .userDetailsService(userDetailsService)
            // Ajouter le filtre JWT avant le filtre d'authentification par nom d'utilisateur/mot de passe
            // Le filtre JWT vérifiera d'abord s'il y a un token, sinon la session sera utilisée
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}


