package com.hospital.management.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.hospital.management.entity.Report;
import com.hospital.management.repository.ReportRepository;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({ReportService.class, UserService.class})
class ReportServiceTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportService reportService;

    @Test
    void shouldReturnSentReportsForPatient() {
        reportRepository.save(new Report(
                "Cardiology Review",
                "Cardiology",
                "John Smith",
                "Approved",
                "Cardiovascular assessment is stable.",
                true,
                "2026-08-28"
        ));

        reportRepository.save(new Report(
                "Neurology Assessment",
                "Neurology",
                "Alice Johnson",
                "Pending",
                "Neurological symptoms require further review.",
                false,
                "2026-08-24"
        ));

        List<Report> reports = reportService.findSentReportsByPatientName("John Smith");

        assertEquals(1, reports.size());
        assertEquals("Cardiology Review", reports.get(0).getTitle());
    }
}
