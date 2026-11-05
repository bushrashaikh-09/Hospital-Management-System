package com.hospital.management.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hospital.management.entity.Report;
import com.hospital.management.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> findAll() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Report> findById(Long id) {
        return reportRepository.findById(id);
    }

    public List<Report> findSentReportsByPatientName(String patientName) {
        if (patientName == null || patientName.isBlank()) {
            return List.of();
        }
        return reportRepository.findBySentToUserTrueAndPatientNameIgnoreCase(patientName.trim());
    }

    public long countAll() {
        return reportRepository.count();
    }

    public boolean existsByTitleAndPatientName(String title, String patientName) {
        return reportRepository.existsByTitleAndPatientName(title, patientName);
    }

    public Report save(Report report) {
        if (report == null) {
            throw new IllegalArgumentException("Report is required");
        }
        return reportRepository.save(report);
    }

    public void deleteById(Long id) {
        reportRepository.deleteById(id);
    }
}
