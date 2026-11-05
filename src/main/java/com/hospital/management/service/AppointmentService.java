package com.hospital.management.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hospital.management.entity.Appointment;
import com.hospital.management.repository.AppointmentRepository;

@Service
public class AppointmentService {

    private static final List<String> DEFAULT_TIME_SLOTS = List.of(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00"
    );

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public long countAll() {
        return appointmentRepository.count();
    }

    public Appointment save(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment is required");
        }
        return appointmentRepository.save(appointment);
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public void deleteById(Long id) {
        appointmentRepository.deleteById(id);
    }

    public boolean hasConflictingAppointment(String doctorName, LocalDate appointmentDate, LocalTime appointmentTime) {
        return hasConflictingAppointment(doctorName, appointmentDate, appointmentTime, null);
    }

    public boolean hasConflictingAppointment(String doctorName, LocalDate appointmentDate,
                                             LocalTime appointmentTime, Long excludedAppointmentId) {
        if (doctorName == null || doctorName.isBlank() || appointmentDate == null || appointmentTime == null) {
            return false;
        }

        return appointmentRepository.findAll().stream()
                .filter(appointment -> appointment != null)
                .filter(appointment -> excludedAppointmentId == null || !excludedAppointmentId.equals(appointment.getId()))
                .filter(appointment -> doctorName.equalsIgnoreCase(appointment.getDoctorName()))
                .filter(appointment -> appointmentDate.equals(appointment.getAppointmentDate()))
                .filter(appointment -> appointmentTime.equals(appointment.getAppointmentTime()))
                .filter(appointment -> !"Cancelled".equalsIgnoreCase(appointment.getStatus()))
                .findAny()
                .isPresent();
    }

    public List<String> getAvailableTimeSlots(String doctorName, LocalDate appointmentDate) {
        if (doctorName == null || doctorName.isBlank() || appointmentDate == null) {
            return new ArrayList<>();
        }

        List<String> availableSlots = new ArrayList<>();
        for (String slot : DEFAULT_TIME_SLOTS) {
            LocalTime slotTime = LocalTime.parse(slot);
            if (appointmentDate.isBefore(LocalDate.now()) || (!appointmentDate.equals(LocalDate.now()) && appointmentDate.isAfter(LocalDate.now()))) {
                if (!hasConflictingAppointment(doctorName, appointmentDate, slotTime)) {
                    availableSlots.add(slot);
                }
            } else if (slotTime.isAfter(LocalTime.now())) {
                if (!hasConflictingAppointment(doctorName, appointmentDate, slotTime)) {
                    availableSlots.add(slot);
                }
            }
        }
        return availableSlots;
    }
}
