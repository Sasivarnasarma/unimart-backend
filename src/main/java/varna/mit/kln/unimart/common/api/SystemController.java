package varna.mit.kln.unimart.common.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SystemController {

    private final JdbcTemplate jdbcTemplate;

    public SystemController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
            "service", "unimart-backend",
            "status", "UP",
            "time", Instant.now()
        );
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        String dbStatus = "DISCONNECTED";
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                dbStatus = "CONNECTED";
            }
        } catch (Exception e) {
            // Keep status as DISCONNECTED on database connection failure
        }

        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("service", "unimart-backend");
        healthInfo.put("status", "UP");
        healthInfo.put("database", dbStatus);
        healthInfo.put("time", Instant.now());

        return healthInfo;
    }
}
