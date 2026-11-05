package com.hospital.management.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.hospital.management.entity.Report;
import com.hospital.management.entity.User;
import com.hospital.management.repository.ReportRepository;
import com.hospital.management.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.mock.web.MockHttpSession;

class ReportControllerTest {

    @Test
    void shouldPersistDescriptionWhenUpdatingReport() {
        ReportRepository reportRepository = mock(ReportRepository.class);
        ReportService reportService = new ReportService(reportRepository);
        ReportController reportController = new ReportController(reportService);

        Report existing = new Report(
                "Old Title",
                "Cardiology",
                "John Smith",
                "Approved",
                "Old summary",
                false,
                "2026-08-28"
        );
        existing.setId(1L);
        existing.setDescription("Old diagnosis details");
        existing.setSummary("Old summary");

        Report incoming = new Report(
                "New Title",
                "Cardiology",
                "John Smith",
                "Approved",
                "New diagnosis details",
                true,
                "2026-08-30"
        );
        incoming.setId(1L);
        incoming.setDescription("New diagnosis details");
        incoming.setSummary("New diagnosis details");

        when(reportRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Report updated = reportController.updateReport(1L, incoming);

        assertEquals("New diagnosis details", updated.getDescription());
        assertEquals("New diagnosis details", updated.getSummary());
        assertFalse(updated.isSentToUser());
    }

    @Test
    void shouldOnlyMarkReportAsSentWhenAdminUsesSendAction() {
        ReportRepository reportRepository = mock(ReportRepository.class);
        ReportService reportService = new ReportService(reportRepository);
        ReportController reportController = new ReportController(reportService);
        Report report = new Report("Visit report", "Cardiology", "John Smith", "Approved",
                "Visit details", true, "2026-08-30");
        report.setId(2L);
        User admin = new User();
        admin.setRole("ADMIN");
        HttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", admin);

        when(reportRepository.findById(2L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Report sent = reportController.sendReportToUser(2L, session);

        assertTrue(sent.isSentToUser());
    }

    @Test
    void shouldKeepNewReportUnsentWhenCreated() {
        ReportRepository reportRepository = mock(ReportRepository.class);
        ReportController reportController = new ReportController(new ReportService(reportRepository));
        Report report = new Report("Visit report", "Cardiology", "John Smith", "Approved",
                "Visit details", true, "2026-08-30");
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Report saved = reportController.createReport(report);

        assertFalse(saved.isSentToUser());
    }
}
