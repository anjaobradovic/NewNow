package rs.ftn.newnow.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import rs.ftn.newnow.dto.AuditLogDTO;
import rs.ftn.newnow.dto.AuditLogPageResponse;
import rs.ftn.newnow.model.AuditLog;
import rs.ftn.newnow.model.enums.AuditAction;
import rs.ftn.newnow.repository.AuditLogRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void logAction(AuditAction action, String actor, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setActor(actor);
            auditLog.setDetails(details);
            auditLog.setIpAddress(getClientIpAddress());
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} by {}", action, actor);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    @Transactional(readOnly = true)
    public AuditLogPageResponse getAuditLogs(String action, String actor, int page, int size) {
        AuditAction auditAction = null;
        if (action != null && !action.isEmpty()) {
            try {
                auditAction = AuditAction.valueOf(action.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid audit action: {}", action);
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logPage = auditLogRepository.findByFilters(auditAction, actor, pageable);
        
        List<AuditLogDTO> logDTOs = logPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new AuditLogPageResponse(
                logDTOs,
                logPage.getNumber(),
                logPage.getTotalPages(),
                logPage.getTotalElements()
        );
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not determine client IP address", e);
        }
        return "unknown";
    }

    private AuditLogDTO convertToDTO(AuditLog auditLog) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(auditLog.getId());
        dto.setAction(auditLog.getAction().name());
        dto.setActor(auditLog.getActor());
        dto.setDetails(auditLog.getDetails());
        dto.setTimestamp(auditLog.getTimestamp());
        dto.setIpAddress(auditLog.getIpAddress());
        return dto;
    }
}
