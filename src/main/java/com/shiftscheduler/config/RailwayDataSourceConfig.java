package com.shiftscheduler.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("railway")
public class RailwayDataSourceConfig {

    private final Environment environment;

    public RailwayDataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() {
        String databaseUrl = environment.getProperty("DATABASE_URL");

        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set. Make sure you have added PostgreSQL database to your Railway project.");
        }

        try {
            // Parse the DATABASE_URL
            URI dbUri = new URI(databaseUrl);

            String host = dbUri.getHost();
            int port = dbUri.getPort();

            return getHikariDataSource(dbUri, host, port);

        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid DATABASE_URL format: " + databaseUrl, e);
        }
    }

    private static HikariDataSource getHikariDataSource(URI dbUri, String host, int port) {
        String database = dbUri.getPath().substring(1); // Remove leading '/'
        String[] userInfo = dbUri.getUserInfo().split(":");
        String username = userInfo[0];
        String password = userInfo.length > 1 ? userInfo[1] : "";

        // Construct proper JDBC URL
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);

        // Create HikariDataSource with proper configuration
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("org.postgresql.Driver");

        // Railway-optimized connection pool settings
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setIdleTimeout(300000);
        dataSource.setMaxLifetime(1200000);
        dataSource.setConnectionTimeout(30000);
        return dataSource;
    }
}
