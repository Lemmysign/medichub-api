package com.medichub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Context smoke test. Runs under the {@code test} profile (in-memory H2), so it verifies the whole
 * Spring context wires up without needing a running Postgres — see {@code application-test.properties}.
 */
@SpringBootTest
@ActiveProfiles("test")
class MedichubApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
