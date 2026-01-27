package oh.grab_web.service.auth;

import lombok.RequiredArgsConstructor;
import oh.grab_web.domain.user.User;
import oh.grab_web.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글에서 유저 정보 가져오기
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. 서비스 구분을 위한 ID (google)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. OAuth2 로그인 진행 시 키가 되는 필드값 (PK와 같은 의미, 구글은 "sub")
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 4. 유저 정보 추출 (Attributes)
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub"); // 구글의 고유 ID

        // 5. DB 저장 또는 업데이트 (핵심 로직)
        User user = saveOrUpdate(email, name, registrationId, providerId);

        // 6. 세션에 저장할 UserPrincipal 반환 (여기서는 간단히 DefaultOAuth2User 사용)
        // 실제로는 email을 key로 하여 세션에서 DB ID를 찾을 수 있도록 구성합니다.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName
        );
    }

    private User saveOrUpdate(String email, String name, String provider, String providerId) {
        User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(name)) // 이미 있으면 이름 업데이트
                .orElse(User.builder()
                        .name(name)
                        .email(email)
                        .provider(provider)
                        .providerId(providerId)
                        .build()); // 없으면 생성

        return userRepository.save(user);
    }
}