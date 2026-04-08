package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.entity.FaqEntry;
import com.hacettepe.iwms.repository.FaqEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR','COORDINATOR','ADMIN')")
public class SupportController {
    private final FaqEntryRepository faqEntryRepository;

    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<List<FaqEntry>>> getFaqs() {
        return ResponseEntity.ok(new ApiResponse<>(true, "FAQ list.", faqEntryRepository.findAll()));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<FaqEntry>>> autocomplete(@RequestParam String query) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Autocomplete results.", faqEntryRepository.findByQuestionContainingIgnoreCase(query)));
    }

    @GetMapping("/chatbot")
    public ResponseEntity<ApiResponse<String>> chatbot(@RequestParam String question) {
        String fallback = "I could not find an exact answer for: " + question + ". Please contact internship coordinator by email.";
        return ResponseEntity.ok(new ApiResponse<>(true, "Chatbot response.", fallback));
    }
}
