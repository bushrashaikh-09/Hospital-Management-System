package com.hospital.management.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hospital.management.entity.Appointment;
import com.hospital.management.entity.Report;
import com.hospital.management.entity.User;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.service.ReportService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ReportService reportService;

    public AppointmentController(AppointmentService appointmentService, ReportService reportService) {
        this.appointmentService = appointmentService;
        this.reportService = reportService;
    }

    @GetMapping("/appointments")
    public List<Appointment> getAppointments(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        if (loggedInUser instanceof User user && "USER".equals(user.getRole())) {
            String fullName = user.getFullName();
            if (fullName != null && !fullName.isBlank()) {
                return appointmentService.findAll().stream()
                        .filter(appointment -> fullName.equalsIgnoreCase(appointment.getPatientName()))
                        .toList();
            }
            return List.of();
        }
        return appointmentService.findAll();
    }

    @GetMapping("/appointments/availability")
    public Map<String, Object> getAvailableTimeSlots(@RequestParam String doctorName,
                                                    @RequestParam String date) {
        LocalDate appointmentDate = LocalDate.parse(date);
        List<String> slots = appointmentService.getAvailableTimeSlots(doctorName, appointmentDate);
        Map<String, Object> response = new HashMap<>();
        response.put("doctorName", doctorName);
        response.put("date", date);
        response.put("availableSlots", slots);
        return response;
    }

    @PostMapping("/appointments")
    public Appointment createAppointment(@RequestBody Appointment appointment) {
        validateAppointment(appointment, true);
        Appointment saved = appointmentService.save(appointment);
        createConfirmationReport(saved);
        return saved;
    }

    @PutMapping("/appointments/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id,
                                                         @RequestBody Appointment appointment) {
        return appointmentService.findById(id)
                .map(existing -> {
                    validateAppointment(appointment, false, id);
                    boolean wasConfirmed = "Confirmed".equalsIgnoreCase(existing.getStatus());
                    existing.setPatientName(appointment.getPatientName());
                    existing.setDoctorName(appointment.getDoctorName());
                    existing.setAppointmentDate(appointment.getAppointmentDate());
                    existing.setAppointmentTime(appointment.getAppointmentTime());
                    existing.setDepartment(appointment.getDepartment());
                    existing.setStatus(normalizeStatus(appointment.getStatus()));
                    existing.setNotes(appointment.getNotes());
                    Appointment saved = appointmentService.save(existing);
                    if (!wasConfirmed) createConfirmationReport(saved);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private void validateAppointment(Appointment appointment, boolean isCreate) {
        validateAppointment(appointment, isCreate, null);
    }

    private void validateAppointment(Appointment appointment, boolean isCreate, Long appointmentId) {
        if (appointment == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment payload is required");
        }
        if (appointment.getPatientName() == null || appointment.getPatientName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient name is required");
        }
        if (appointment.getDoctorName() == null || appointment.getDoctorName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor name is required");
        }
        if (appointment.getAppointmentDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment date is required");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment time is required");
        }
        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past dates cannot be booked");
        }
        if (appointmentService.hasConflictingAppointment(
                appointment.getDoctorName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(), appointmentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This doctor is already booked at the selected time");
        }
        if (isCreate && (appointment.getStatus() == null || appointment.getStatus().isBlank())) {
            appointment.setStatus("Pending");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "Pending";
        return switch (status.trim().toLowerCase()) {
            case "confirmed" -> "Confirmed";
            case "completed" -> "Completed";
            case "cancelled" -> "Cancelled";
            default -> "Pending";
        };
    }

    @DeleteMapping("/appointments/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteById(id);
    }

    private void createConfirmationReport(Appointment appointment) {
        if (!"Confirmed".equalsIgnoreCase(appointment.getStatus())) return;
        String title = appointment.getDepartment() + " Appointment Confirmation";
        if (reportService.existsByTitleAndPatientName(title, appointment.getPatientName())) return;
        Report report = new Report(title, appointment.getDepartment(), appointment.getPatientName(),
                "Approved", "Appointment confirmed with " + appointment.getDoctorName() + ".", true,
                appointment.getAppointmentDate().toString());
        reportService.save(report);
    }
}
