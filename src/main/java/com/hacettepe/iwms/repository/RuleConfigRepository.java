package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RuleConfigRepository extends JpaRepository<RuleConfig, Long> {
    Optional<RuleConfig> findByRuleKey(String ruleKey);
}
