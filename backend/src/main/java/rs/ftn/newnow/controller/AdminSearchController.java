package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ftn.newnow.search.SearchIndexService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/search")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminSearchController {

    private final SearchIndexService searchIndexService;

    @PostMapping("/reindex")
    public ResponseEntity<?> reindex() {
        int indexed = searchIndexService.bulkReindex();
        return ResponseEntity.ok(Map.of("indexed", indexed));
    }
}
