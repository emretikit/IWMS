package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.service.AuditService;
import com.hacettepe.iwms.service.GenericEntityCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/entities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEntityCrudController {

    private final GenericEntityCrudService genericEntityCrudService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<Set<String>>> listEntities() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Supported entities listed.", genericEntityCrudService.supportedEntities()));
    }

    @PutMapping("/{entity}/{id}")
    public ResponseEntity<ApiResponse<Object>> updateEntity(@PathVariable String entity,
                                                            @PathVariable String id,
                                                            @RequestBody Map<String, Object> payload,
                                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Object updated = genericEntityCrudService.update(entity, id, payload);
        String normalizedEntity = genericEntityCrudService.normalizeEntityName(entity);
        auditService.log(currentUser.getId(), "GENERIC_ENTITY_UPDATE", normalizedEntity.toUpperCase(), parseLongOrNull(id), "updated via generic admin endpoint");
        return ResponseEntity.ok(new ApiResponse<>(true, normalizedEntity + " updated.", updated));
    }

    @DeleteMapping("/{entity}/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEntity(@PathVariable String entity,
                                                          @PathVariable String id,
                                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        String normalizedEntity = genericEntityCrudService.normalizeEntityName(entity);
        genericEntityCrudService.delete(entity, id);
        auditService.log(currentUser.getId(), "GENERIC_ENTITY_DELETE", normalizedEntity.toUpperCase(), parseLongOrNull(id), "deleted via generic admin endpoint");
        return ResponseEntity.ok(new ApiResponse<>(true, normalizedEntity + " deleted.", null));
    }

    private Long parseLongOrNull(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
