package oh.grab_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot 애플리케이션의 시작점입니다.
 * 이 파일이 있어야 테스트 코드(@DataJpaTest)가 설정을 읽어올 수 있습니다.
 */
@EnableJpaAuditing // BaseTimeEntity(생성일/수정일) 자동 입력을 위해 필수
@SpringBootApplication
public class GrabWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrabWebApplication.class, args);
	}
}