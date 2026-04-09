package com.hacettepe.iwms.config;

import com.hacettepe.iwms.entity.InternshipStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void synchronizeInternshipStatusConstraint() {
        String allowedValues = Arrays.stream(InternshipStatus.values())
                .map(status -> "'" + status.name() + "'")
                .collect(Collectors.joining(", "));

        jdbcTemplate.execute("ALTER TABLE internship DROP CONSTRAINT IF EXISTS internship_status_check");
        jdbcTemplate.execute(
                "ALTER TABLE internship ADD CONSTRAINT internship_status_check " +
                        "CHECK (status IN (" + allowedValues + "))"
        );

        log.info("Synchronized internship_status_check constraint with enum values: {}", allowedValues);
    }
}
