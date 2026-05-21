package com.hacettepe.iwms.config;

import com.hacettepe.iwms.entity.InternshipCoordinator;
import com.hacettepe.iwms.entity.Role;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.repository.InternshipCoordinatorRepository;
import com.hacettepe.iwms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(50)
public class DemoUserSeeder {

    private static final String COORDINATOR_USERNAME = "coordinator-demo";
    private static final String COORDINATOR_EMAIL = "coordinator-demo@hacettepe.edu.tr";
    private static final String COORDINATOR_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final InternshipCoordinatorRepository internshipCoordinatorRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDemoUsers() {
        seedCoordinatorDemo();
    }

    private void seedCoordinatorDemo() {
        if (userRepository.findByUsername(COORDINATOR_USERNAME).isPresent()) {
            log.info("Demo coordinator '{}' already exists, skipping seed.", COORDINATOR_USERNAME);
            return;
        }

        User user = User.builder()
                .username(COORDINATOR_USERNAME)
                .email(COORDINATOR_EMAIL)
                .passwordHash(passwordEncoder.encode(COORDINATOR_PASSWORD))
                .role(Role.COORDINATOR)
                .name("Demo")
                .surname("Coordinator")
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);

        InternshipCoordinator coordinator = InternshipCoordinator.builder()
                .user(savedUser)
                .build();
        internshipCoordinatorRepository.save(coordinator);

        log.info("Seeded demo coordinator user: username='{}' password='{}'", COORDINATOR_USERNAME, COORDINATOR_PASSWORD);
    }
}
