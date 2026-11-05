package com.hospital.management.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.hospital.management.entity.Report;
import com.hospital.management.entity.User;
import com.hospital.management.service.ReportService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public List<Report> getReports(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        if (loggedInUser instanceof User user && "USER".equals(user.getRole())) {
            String fullName = user.getFullName();
            if (fullName != null && !fullName.isBlank()) {
                return reportService.findSentReportsByPatientName(fullName);
            }
            return List.of();
        }
        return reportService.findAll();
    }

    @GetMapping("/reports/patient/{patientName}")
    public List<Report> getReportsForPatient(@PathVariable String patientName) {
        return reportService.findSentReportsByPatientName(patientName);
    }

    @PostMapping("/reports")
    public Report createReport(@RequestBody Report report) {
        report.setSentToUser(false);
        return reportService.save(report);
    }

    @PostMapping("/reports/{id}/send")
    public Report sendReportToUser(@PathVariable Long id, HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        if (!(loggedInUser instanceof User user) || !"ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can send reports");
        }

        Report report = reportService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
        report.setSentToUser(true);
        return reportService.save(report);
    }

    @PutMapping("/reports/{id}")
    public Report updateReport(@PathVariable Long id, @RequestBody Report report) {
        Report existing = reportService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));

        existing.setTitle(report.getTitle());
        existing.setDepartment(report.getDepartment());
        existing.setPatientName(report.getPatientName());
        existing.setStatus(report.getStatus());
        existing.setDescription(report.getDescription() != null ? report.getDescription() : report.getSummary());
        existing.setSummary(report.getSummary() != null ? report.getSummary() : report.getDescription());
        existing.setReportDate(report.getReportDate());
        return reportService.save(existing);
    }

    @DeleteMapping("/reports/{id}")
    public void deleteReport(@PathVariable Long id) {
        reportService.deleteById(id);
    }
}
