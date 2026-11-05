package com.hospital.management.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospital.management.entity.Appointment;
import com.hospital.management.repository.AppointmentRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void shouldDetectBookedDoctorSlot() {
        Appointment existing = new Appointment();
        existing.setDoctorName("Dr. Rahul Mehta");
        existing.setAppointmentDate(LocalDate.of(2026, 9, 20));
        existing.setAppointmentTime(LocalTime.of(10, 0));
        existing.setStatus("Confirmed");

        when(appointmentRepository.findAll()).thenReturn(List.of(existing));

        assertTrue(appointmentService.hasConflictingAppointment(
                "Dr. Rahul Mehta",
                LocalDate.of(2026, 9, 20),
                LocalTime.of(10, 0)
        ));
    }

    @Test
    void shouldAllowDifferentDoctorAtSameTime() {
        Appointment existing = new Appointment();
        existing.setDoctorName("Dr. Priya Shah");
        existing.setAppointmentDate(LocalDate.of(2026, 9, 20));
        existing.setAppointmentTime(LocalTime.of(10, 0));
        existing.setStatus("Confirmed");

        when(appointmentRepository.findAll()).thenReturn(List.of(existing));

        assertFalse(appointmentService.hasConflictingAppointment(
                "Dr. Rahul Mehta",
                LocalDate.of(2026, 9, 20),
                LocalTime.of(10, 0)
        ));
    }
}
