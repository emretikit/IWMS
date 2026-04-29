package com.hacettepe.iwms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenericEntityCrudService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    @Transactional
    public Object update(String entityName, String rawId, Map<String, Object> payload) {
        EntityType<?> entityType = resolveEntityType(entityName);
        Object id = convertId(entityType, rawId);
        Object existing = entityManager.find(entityType.getJavaType(), id);
        if (existing == null) {
            throw new ResourceNotFoundException(entityType.getName() + " not found for id " + rawId);
        }

        Map<String, Object> safePayload = new HashMap<>(payload);
        safePayload.remove(getIdAttributeName(entityType));
        try {
            objectMapper.updateValue(existing, safePayload);
        } catch (Exception ex) {
            throw new ValidationException("Invalid update payload for entity " + entityType.getName() + ": " + ex.getMessage());
        }

        return entityManager.merge(existing);
    }

    @Transactional
    public void delete(String entityName, String rawId) {
        EntityType<?> entityType = resolveEntityType(entityName);
        Object id = convertId(entityType, rawId);
        Object existing = entityManager.find(entityType.getJavaType(), id);
        if (existing == null) {
            throw new ResourceNotFoundException(entityType.getName() + " not found for id " + rawId);
        }
        entityManager.remove(existing);
    }

    public String normalizeEntityName(String entityName) {
        return resolveEntityType(entityName).getName();
    }

    public Set<String> supportedEntities() {
        return entityManager.getMetamodel()
                .getEntities()
                .stream()
                .map(EntityType::getName)
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    private EntityType<?> resolveEntityType(String entityName) {
        String normalized = entityName.toLowerCase(Locale.ROOT);
        Metamodel metamodel = entityManager.getMetamodel();

        for (EntityType<?> type : metamodel.getEntities()) {
            if (type.getName().toLowerCase(Locale.ROOT).equals(normalized)
                    || type.getJavaType().getSimpleName().toLowerCase(Locale.ROOT).equals(normalized)) {
                return type;
            }
        }

        throw new ValidationException("Unsupported entity: " + entityName);
    }

    private Object convertId(EntityType<?> entityType, String rawId) {
        Class<?> idType = entityType.getIdType().getJavaType();
        try {
            if (Long.class.equals(idType) || long.class.equals(idType)) {
                return Long.valueOf(rawId);
            }
            if (Integer.class.equals(idType) || int.class.equals(idType)) {
                return Integer.valueOf(rawId);
            }
            if (String.class.equals(idType)) {
                return rawId;
            }
            throw new ValidationException("Unsupported id type for entity " + entityType.getName() + ": " + idType.getSimpleName());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Invalid id value '" + rawId + "' for entity " + entityType.getName());
        } catch (IllegalArgumentException | EntityNotFoundException ex) {
            throw new ValidationException("Invalid id value '" + rawId + "' for entity " + entityType.getName());
        }
    }

    private String getIdAttributeName(EntityType<?> entityType) {
        Class<?> idType = entityType.getIdType().getJavaType();
        return entityType.getId(idType).getName();
    }
}
