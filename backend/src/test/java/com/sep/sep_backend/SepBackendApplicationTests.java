package com.sep.sep_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        // Deaktiviert data.sql, damit wir keine Konflikte beim Start haben
        "spring.sql.init.mode=never",
        // Dummy API Key, damit der Context nicht meckert
        "gemini.api.key=dummy-test-key"
})
class SepBackendApplicationTests {

    @Test
    void contextLoads() {
        // Prüft nur, ob die App ohne Fehler starten kann
    }
}