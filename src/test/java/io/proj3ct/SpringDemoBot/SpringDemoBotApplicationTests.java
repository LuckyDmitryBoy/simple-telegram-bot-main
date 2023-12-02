package io.proj3ct.SpringDemoBot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:./application.properties")
class SpringDemoBotApplicationTests {

	@Test
	void contextLoads() {
	}

}
