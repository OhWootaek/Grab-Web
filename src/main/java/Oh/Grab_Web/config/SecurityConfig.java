package oh.grab_web.config;

import lombok.RequiredArgsConstructor;
import oh.grab_web.service.auth.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 해제 (개발 편의성을 위해, 실제 운영 시에는 켜는 것이 좋음)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. HTTP 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll() // 메인, 정적 리소스는 누구나 접근 가능
                        .requestMatchers("/api/**").authenticated() // API 요청은 로그인 필요
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )

                // 3. H2 Console 사용을 위한 설정 (Frame Options 해제)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 4. OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 우리가 만든 서비스 등록
                        )
                        .defaultSuccessUrl("/", true) // 로그인 성공 시 메인으로 이동
                );

        return http.build();
    }
}