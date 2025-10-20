package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.ftn.newnow.dto.HealthResponse;
import rs.ftn.newnow.dto.VersionResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemService {

    private final DataSource dataSource;

    @Value("${app.version:0.0.1-SNAPSHOT}")
    private String appVersion;

    @Value("${app.commit:unknown}")
    private String appCommit;

    @Value("${app.build-time:unknown}")
    private String buildTime;

    public HealthResponse getHealthStatus() {
        String dbStatus = checkDatabaseConnection();
        String overallStatus = "UP".equals(dbStatus) ? "UP" : "DOWN";
        
        return new HealthResponse(overallStatus, dbStatus, Instant.now().toEpochMilli());
    }

    public VersionResponse getVersionInfo() {
        return new VersionResponse(appVersion, appCommit, buildTime);
    }

    private String checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(1) ? "UP" : "DOWN";
        } catch (Exception e) {
            log.error("Database connection check failed", e);
            return "DOWN";
        }
    }
}
