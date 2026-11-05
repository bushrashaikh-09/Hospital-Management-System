package com.hospital.management.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class AppointmentTest {

    @Test
    void prePersist_shouldNormalizeAppointmentValues() {
        Appointment appointment = new Appointment();
        appointment.setPatientName("  John Smith  ");
        appointment.setDoctorName("  Dr. Rahul Mehta  ");
        appointment.setAppointmentDate(LocalDate.of(2026, 9, 2));
        appointment.setAppointmentTime(LocalTime.of(10, 30));
        appointment.setDepartment("  Cardiology  ");
        appointment.setStatus("pending");
        appointment.setNotes("  Follow-up  ");

        appointment.prePersist();

        assertEquals("John Smith", appointment.getPatientName());
        assertEquals("Dr. Rahul Mehta", appointment.getDoctorName());
        assertEquals("Cardiology", appointment.getDepartment());
        assertEquals("Pending", appointment.getStatus());
        assertEquals("Follow-up", appointment.getNotes());
    }
}
