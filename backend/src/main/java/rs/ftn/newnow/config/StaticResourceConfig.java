package rs.ftn.newnow.config;

import org.springframework.context.annotation.Configuration;

/**
 * Static file serving has been migrated to MinIO. See {@code controller.UploadProxyController}
 * which streams /uploads/** from MinIO.
 */
@Configuration
public class StaticResourceConfig {
}
