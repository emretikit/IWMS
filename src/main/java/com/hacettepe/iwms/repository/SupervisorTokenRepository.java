package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.SupervisorToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorTokenRepository extends JpaRepository<SupervisorToken, Long> {
    Optional<SupervisorToken> findByToken(String token);
    List<SupervisorToken> findByInternshipId(Long internshipId);
}
