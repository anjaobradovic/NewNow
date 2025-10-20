package rs.ftn.newnow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import rs.ftn.newnow.dto.HealthResponse;
import rs.ftn.newnow.dto.VersionResponse;
import rs.ftn.newnow.service.SystemService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemControllerTest {

    @Mock
    private SystemService systemService;

    @InjectMocks
    private SystemController systemController;

    private HealthResponse healthResponse;
    private VersionResponse versionResponse;

    @BeforeEach
    void setUp() {
        healthResponse = new HealthResponse("UP", "UP", System.currentTimeMillis());
        versionResponse = new VersionResponse("0.0.1-SNAPSHOT", "abc123", "2025-10-20");
    }

    @Test
    void healthCheck_ReturnsHealthStatus() {
        when(systemService.getHealthStatus()).thenReturn(healthResponse);

        ResponseEntity<HealthResponse> response = systemController.healthCheck();

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("UP", response.getBody().getStatus());
        verify(systemService, times(1)).getHealthStatus();
    }

    @Test
    void getVersion_ReturnsVersionInfo() {
        when(systemService.getVersionInfo()).thenReturn(versionResponse);

        ResponseEntity<VersionResponse> response = systemController.getVersion();

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("0.0.1-SNAPSHOT", response.getBody().getVersion());
        assertEquals("abc123", response.getBody().getCommit());
        verify(systemService, times(1)).getVersionInfo();
    }
}
