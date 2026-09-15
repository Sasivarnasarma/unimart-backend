package varna.mit.kln.unimart.common.api;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> rootInfo() {
        return Map.of(
            "service", "UniMart Backend API",
            "version", "1.0.0",
            "status", "UP",
            "swaggerUi", "/swagger-ui/index.html",
            "apiDocs", "/v3/api-docs",
            "healthCheck", "/api/v1/public/health",
            "timestamp", Instant.now()
        );
    }
}
