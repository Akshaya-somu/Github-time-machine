package com.githubtimemachine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI githubTimeMachineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GitHub Time Machine API")
                        .description("REST API for managing GitHub repository snapshots and historical views.")
                        .version("v1.0.0"));
    }
}
