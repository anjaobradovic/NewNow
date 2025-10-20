package rs.ftn.newnow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import rs.ftn.newnow.dto.AuditLogPageResponse;
import rs.ftn.newnow.model.AuditLog;
import rs.ftn.newnow.model.enums.AuditAction;
import rs.ftn.newnow.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setAction(AuditAction.USER_LOGIN);
        auditLog.setActor("test@example.com");
        auditLog.setDetails("User logged in successfully");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setIpAddress("127.0.0.1");
    }

    @Test
    void logAction_SavesAuditLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        auditLogService.logAction(AuditAction.USER_LOGIN, "test@example.com", "User logged in");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, timeout(1000).times(1)).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals(AuditAction.USER_LOGIN, savedLog.getAction());
        assertEquals("test@example.com", savedLog.getActor());
    }

    @Test
    void getAuditLogs_WithNoFilters_ReturnsAllLogs() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
        when(auditLogRepository.findByFilters(any(), any(), any(Pageable.class))).thenReturn(page);

        AuditLogPageResponse response = auditLogService.getAuditLogs(null, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getLogs().size());
        assertEquals("USER_LOGIN", response.getLogs().get(0).getAction());
        verify(auditLogRepository, times(1)).findByFilters(any(), any(), any(Pageable.class));
    }

    @Test
    void getAuditLogs_WithActionFilter_ReturnsFilteredLogs() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
        when(auditLogRepository.findByFilters(eq(AuditAction.USER_LOGIN), any(), any(Pageable.class)))
                .thenReturn(page);

        AuditLogPageResponse response = auditLogService.getAuditLogs("USER_LOGIN", null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getLogs().size());
        assertEquals("USER_LOGIN", response.getLogs().get(0).getAction());
    }

    @Test
    void getAuditLogs_WithActorFilter_ReturnsFilteredLogs() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
        when(auditLogRepository.findByFilters(any(), eq("test@example.com"), any(Pageable.class)))
                .thenReturn(page);

        AuditLogPageResponse response = auditLogService.getAuditLogs(null, "test@example.com", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getLogs().size());
        assertEquals("test@example.com", response.getLogs().get(0).getActor());
    }

    @Test
    void getAuditLogs_WithInvalidAction_HandlesGracefully() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
        when(auditLogRepository.findByFilters(any(), any(), any(Pageable.class))).thenReturn(page);

        AuditLogPageResponse response = auditLogService.getAuditLogs("INVALID_ACTION", null, 0, 10);

        assertNotNull(response);
        verify(auditLogRepository, times(1)).findByFilters(isNull(), any(), any(Pageable.class));
    }
}
