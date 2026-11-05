package com.hospital.management.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hospital.management.entity.Appointment;
import com.hospital.management.entity.Invoice;
import com.hospital.management.repository.InvoiceRepository;

class InvoiceServiceTest {

    @Test
    void generateInvoiceNumber_shouldUseInvoicePrefixAndYear() {
        String invoiceNumber = InvoiceService.generateInvoiceNumber(2026L, 1L);
        assertEquals("INV-2026-0001", invoiceNumber);
    }

    @Test
    void shouldTreatPastDueDateAsOverdue() {
        assertTrue(InvoiceService.isOverdue(LocalDate.now().minusDays(2), "Pending"));
    }

    @Test
    void shouldGenerateInvoiceFromAppointment() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(invoiceRepository);

        Appointment appointment = new Appointment();
        appointment.setPatientName("Jane Smith");
        appointment.setDoctorName("Dr. Rahul Mehta");
        appointment.setDepartment("Cardiology");
        appointment.setAppointmentDate(LocalDate.now().plusDays(3));
        appointment.setStatus("Confirmed");

        when(invoiceRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice invoice = invoiceService.generateInvoiceFromAppointment(appointment);

        assertNotNull(invoice);
        assertEquals("Jane Smith", invoice.getPatientName());
        assertEquals("Dr. Rahul Mehta", invoice.getDoctorName());
        assertEquals("Cardiology", invoice.getDepartment());
        assertEquals(new BigDecimal("2500.00"), invoice.getAmount());
    }
}
