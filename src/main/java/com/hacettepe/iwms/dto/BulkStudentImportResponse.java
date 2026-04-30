package com.hacettepe.iwms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkStudentImportResponse {
    private int totalRows;
    private int createdCount;
    @Builder.Default
    private List<String> skipped = new ArrayList<>();
}
