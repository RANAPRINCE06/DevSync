package com.devsync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DevSyncApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context loads and Flyway migration runs successfully on H2 in-memory database
    }
}
