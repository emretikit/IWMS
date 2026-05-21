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
import java.util.Optional;

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
        // Find an exact match for the question, ignoring case.
        Optional<FaqEntry> faqMatch = faqEntryRepository.findByQuestionContainingIgnoreCase(question)
                .stream()
                .filter(faq -> faq.getQuestion().equalsIgnoreCase(question))
                .findFirst();

        if (faqMatch.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Chatbot response.", faqMatch.get().getAnswer()));
        } else {
            String fallback = "I could not find an exact answer for: '" + question + "'. Please contact your internship coordinator by email.";
            return ResponseEntity.ok(new ApiResponse<>(true, "Chatbot response.", fallback));
        }
    }
}
