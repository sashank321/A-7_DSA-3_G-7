package com.stratasearch.backend.config;

import com.stratasearch.backend.engine.EngineGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfig {
    @Bean
    public EngineGateway EngineGatewayBean() {
        return new EngineGateway();
    }
}
