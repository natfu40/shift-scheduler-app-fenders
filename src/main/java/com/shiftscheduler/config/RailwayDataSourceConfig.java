package com.shiftscheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
@Profile("railway")
public class RailwayDataSourceConfig {

    private final Environment environment;

    public RailwayDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        String databaseUrl = environment.getProperty("DATABASE_URL");

        if (databaseUrl == null) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set. Make sure you have added PostgreSQL database to your Railway project.");
        }

        // Convert Railway's postgresql:// format to jdbc:postgresql:// format
        String jdbcUrl = databaseUrl;
        if (databaseUrl.startsWith("postgresql://")) {
            jdbcUrl = "jdbc:" + databaseUrl;
        }

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .build();
    }
}
