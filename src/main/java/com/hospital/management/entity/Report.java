package com.hospital.management.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "report_type", nullable = false)
    private String reportType = "Medical";

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private String status = "Approved";

    @Column(name = "sent_to_user", nullable = false)
    private boolean sentToUser = false;

    @Column(name = "summary")
    private String summary;

    @Column(name = "report_date", nullable = false)
    private String reportDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.title != null) this.title = this.title.trim();
        if (this.department != null) this.department = this.department.trim();
        if (this.patientName != null) this.patientName = this.patientName.trim();
        if (this.reportType == null || this.reportType.isBlank()) {
            this.reportType = "Medical";
        } else {
            this.reportType = this.reportType.trim();
        }
        if (this.description != null) this.description = this.description.trim();
        if (this.status == null || this.status.isBlank()) {
            this.status = "Approved";
        } else {
            this.status = this.status.trim();
        }
        if (this.summary != null) this.summary = this.summary.trim();
        if (this.summary == null || this.summary.isBlank()) {
            this.summary = this.description;
        }
        if (this.reportDate == null || this.reportDate.isBlank()) {
            this.reportDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public Report() {
    }

    public Report(String title, String department, String patientName, String status, String summary, boolean sentToUser, String reportDate) {
        this.title = title;
        this.department = department;
        this.patientName = patientName;
        this.reportType = "Medical";
        this.description = summary;
        this.status = status;
        this.summary = summary;
        this.sentToUser = sentToUser;
        this.reportDate = reportDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSentToUser() {
        return sentToUser;
    }

    public void setSentToUser(boolean sentToUser) {
        this.sentToUser = sentToUser;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
