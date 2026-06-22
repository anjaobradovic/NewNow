package rs.ftn.newnow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import rs.ftn.newnow.search.SearchIndexService;

@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
public class SearchBootstrap implements CommandLineRunner {

    private final SearchIndexService searchIndexService;

    @Value("${newnow.search.bootstrap-on-startup:true}")
    private boolean bootstrapOnStartup;

    @Override
    public void run(String... args) {
        if (!bootstrapOnStartup) {
            log.info("Search bootstrap disabled (newnow.search.bootstrap-on-startup=false)");
            return;
        }
        try {
            int indexed = searchIndexService.bulkReindex();
            log.info("Search bootstrap indexed {} places", indexed);
        } catch (Exception e) {
            log.error("Search bootstrap failed: {}", e.getMessage());
        }
    }
}
