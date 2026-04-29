package com.hacettepe.iwms.service;

import com.hacettepe.iwms.entity.FaqEntry;
import com.hacettepe.iwms.repository.FaqEntryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class FaqDataInitializer implements CommandLineRunner {

    private final FaqEntryRepository faqEntryRepository;

    public FaqDataInitializer(FaqEntryRepository faqEntryRepository) {
        this.faqEntryRepository = faqEntryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Clear existing FAQs to ensure a clean state for initialization
        // This is useful for development/testing with in-memory databases or frequent restarts
        faqEntryRepository.deleteAll();
        System.out.println("Cleared all existing FAQ entries.");

        List<FaqEntry> initialFaqs = Arrays.asList(
            FaqEntry.builder()
                    .question("How do I submit my internship report?")
                    .answer("You can submit your internship report through the 'Report Panel' section of the student module. Make sure to fill in all required fields and upload your PDF report before the deadline.")
                    .category("Submission")
                    .build(),
            FaqEntry.builder()
                    .question("How to submit report?") // Exact match for chatbot test
                    .answer("To submit your report, navigate to the 'Report Panel' in your student dashboard. Fill out the structured report sections and upload your final PDF document. Ensure all sections are complete and the file is in PDF format.")
                    .category("Submission")
                    .build(),
            FaqEntry.builder()
                    .question("What is the minimum number of internship days required?")
                    .answer("The minimum number of internship days is typically 20 working days, but please check the academic period details for specific requirements.")
                    .category("General")
                    .build(),
            FaqEntry.builder()
                    .question("Where can I find the internship guidelines?")
                    .answer("The official internship guidelines are available on the university's career services portal under the 'Internship Documents' section.")
                    .category("General")
                    .build(),
            FaqEntry.builder()
                    .question("What if my company supervisor does not approve my application?")
                    .answer("If your application is not approved, you will receive feedback from the coordinator. You may need to revise your application or find an alternative company.")
                    .category("Application")
                    .build()
        );

        faqEntryRepository.saveAll(initialFaqs);
        System.out.println("Initialized " + faqEntryRepository.count() + " default FAQ entries.");
    }
}
