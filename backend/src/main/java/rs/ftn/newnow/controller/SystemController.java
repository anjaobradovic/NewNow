package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ftn.newnow.dto.HealthResponse;
import rs.ftn.newnow.dto.VersionResponse;
import rs.ftn.newnow.service.SystemService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class SystemController {

    private final SystemService systemService;

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        log.debug("Health check requested");
        HealthResponse health = systemService.getHealthStatus();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/version")
    public ResponseEntity<VersionResponse> getVersion() {
        log.debug("Version information requested");
        VersionResponse version = systemService.getVersionInfo();
        return ResponseEntity.ok(version);
    }
}
