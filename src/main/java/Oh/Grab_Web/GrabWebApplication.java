package Oh.Grab_Web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GrabWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrabWebApplication.class, args);
	}

}
