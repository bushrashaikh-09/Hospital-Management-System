package com.hospital.management.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hospital.management.entity.Appointment;
import com.hospital.management.entity.Invoice;
import com.hospital.management.repository.InvoiceRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> findAll() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    public Invoice save(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice is required");
        }
        if (invoice.getPatientName() == null || invoice.getPatientName().isBlank()) {
            throw new IllegalArgumentException("Patient name is required");
        }
        if (invoice.getDoctorName() == null || invoice.getDoctorName().isBlank()) {
            throw new IllegalArgumentException("Doctor name is required");
        }
        if (invoice.getDepartment() == null || invoice.getDepartment().isBlank()) {
            throw new IllegalArgumentException("Department is required");
        }
        if (invoice.getAmount() == null) {
            invoice.setAmount(BigDecimal.ZERO);
        }
        if (invoice.getStatus() == null || invoice.getStatus().isBlank()) {
            invoice.setStatus("Pending");
        }
        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDate.now());
        }
        if (invoice.getDueDate() == null) {
            invoice.setDueDate(invoice.getInvoiceDate().plusDays(14));
        }
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(generateInvoiceNumber(
                    Long.valueOf(invoice.getInvoiceDate().getYear()),
                    invoiceRepository.count() + 1
            ));
        }
        return invoiceRepository.save(invoice);
    }

    public void deleteById(Long id) {
        invoiceRepository.deleteById(id);
    }

    public Invoice generateInvoiceFromAppointment(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment is required");
        }
        if (appointment.getPatientName() == null || appointment.getPatientName().isBlank()) {
            throw new IllegalArgumentException("Patient name is required");
        }
        if (appointment.getDoctorName() == null || appointment.getDoctorName().isBlank()) {
            throw new IllegalArgumentException("Doctor name is required");
        }
        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required");
        }
        if ("Cancelled".equalsIgnoreCase(appointment.getStatus())) {
            return null;
        }

        String patientName = appointment.getPatientName().trim();
        String doctorName = appointment.getDoctorName().trim();
        String department = appointment.getDepartment() == null || appointment.getDepartment().isBlank()
                ? "General Consultation" : appointment.getDepartment().trim();

        boolean exists = invoiceRepository.findAllByOrderByCreatedAtDesc().stream()
                .anyMatch(invoice -> invoice.getPatientName() != null
                        && invoice.getDoctorName() != null
                        && invoice.getAppointmentDate() != null
                        && invoice.getPatientName().equalsIgnoreCase(patientName)
                        && invoice.getDoctorName().equalsIgnoreCase(doctorName)
                        && invoice.getAppointmentDate().equals(appointment.getAppointmentDate()));
        if (exists) {
            return invoiceRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(invoice -> invoice.getPatientName() != null
                            && invoice.getDoctorName() != null
                            && invoice.getAppointmentDate() != null
                            && invoice.getPatientName().equalsIgnoreCase(patientName)
                            && invoice.getDoctorName().equalsIgnoreCase(doctorName)
                            && invoice.getAppointmentDate().equals(appointment.getAppointmentDate()))
                    .findFirst()
                    .orElse(null);
        }

        Invoice invoice = new Invoice();
        invoice.setPatientName(patientName);
        invoice.setDoctorName(doctorName);
        invoice.setDepartment(department);
        invoice.setAppointmentDate(appointment.getAppointmentDate());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(14));
        invoice.setAmount(getDepartmentFee(department));
        invoice.setStatus("Pending");
        invoice.setNotes("Generated automatically from appointment booking on "
                + appointment.getAppointmentDate() + " at " + appointment.getAppointmentTime());
        return save(invoice);
    }

    public static String generateInvoiceNumber(Long year, Long sequence) {
        return String.format("INV-%d-%04d", year, sequence);
    }

    public static String normalizeDepartment(String department) {
        if (department == null || department.isBlank()) {
            return "General Consultation";
        }
        return department.trim();
    }

    public static BigDecimal getDepartmentFee(String department) {
        String normalized = normalizeDepartment(department);
        Map<String, BigDecimal> feeMap = Map.of(
                "Cardiology", new BigDecimal("2500.00"),
                "Neurology", new BigDecimal("2800.00"),
                "Dermatology", new BigDecimal("1800.00"),
                "Orthopedics", new BigDecimal("2200.00"),
                "Pediatrics", new BigDecimal("2000.00"),
                "General Consultation", new BigDecimal("1500.00")
        );
        return feeMap.getOrDefault(normalized, new BigDecimal("2000.00"));
    }

    public static boolean isOverdue(LocalDate dueDate, String status) {
        if (dueDate == null) {
            return false;
        }
        if (status == null || status.isBlank()) {
            return false;
        }
        return dueDate.isBefore(LocalDate.now()) && !"Paid".equalsIgnoreCase(status) && !"Cancelled".equalsIgnoreCase(status);
    }
}
