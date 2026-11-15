package com.example.demo;

import org.flywaydb.core.Flyway;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Applies the canonical schema before the MariaDB-backed Spring context is created.
 * This keeps persistence tests independent of the execution order of other tests.
 */
public class MariaDbSchemaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        String url = environment.getRequiredProperty("IMDBOPEN_TEST_DB_URL");
        String username = environment.getRequiredProperty("IMDBOPEN_TEST_DB_USERNAME");
        String password = environment.getRequiredProperty("IMDBOPEN_TEST_DB_PASSWORD");

        Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
