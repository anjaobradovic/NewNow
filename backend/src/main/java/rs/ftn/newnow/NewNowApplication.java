package rs.ftn.newnow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NewNowApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewNowApplication.class, args);
	}

}
