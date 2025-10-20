package rs.ftn.newnow.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.model.AuditLog;
import rs.ftn.newnow.model.enums.AuditAction;
import rs.ftn.newnow.repository.AuditLogRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();

        AuditLog log1 = new AuditLog();
        log1.setAction(AuditAction.USER_LOGIN);
        log1.setActor("admin@example.com");
        log1.setDetails("Admin logged in");
        log1.setTimestamp(LocalDateTime.now());
        log1.setIpAddress("127.0.0.1");
        auditLogRepository.save(log1);

        AuditLog log2 = new AuditLog();
        log2.setAction(AuditAction.LOCATION_CREATED);
        log2.setActor("admin@example.com");
        log2.setDetails("Location created");
        log2.setTimestamp(LocalDateTime.now());
        log2.setIpAddress("127.0.0.1");
        auditLogRepository.save(log2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_AsAdmin_ReturnsLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray())
                .andExpect(jsonPath("$.logs.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_WithActionFilter_ReturnsFilteredLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .param("action", "USER_LOGIN")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray())
                .andExpect(jsonPath("$.logs[0].action").value("USER_LOGIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_WithActorFilter_ReturnsFilteredLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs")
                        .param("actor", "admin@example.com")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray())
                .andExpect(jsonPath("$.logs[0].actor").value("admin@example.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAuditLogs_AsUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAuditLogs_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/audit/logs"))
                .andExpect(status().isForbidden());
    }
}
