package com.example.demo.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI imdbOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("IMDBOpen API")
                        .description("REST API for the IMDBOpen application")
                        .version("1.0.0"));
    }
}
