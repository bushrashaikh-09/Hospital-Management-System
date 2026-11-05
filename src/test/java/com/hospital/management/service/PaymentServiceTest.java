package com.hospital.management.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hospital.management.entity.Invoice;
import com.hospital.management.repository.InvoiceRepository;
import com.hospital.management.repository.PaymentRepository;

@SuppressWarnings("unused")
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        paymentService = new PaymentService(paymentRepository, invoiceRepository);
    }

    @Test
    void shouldGenerateReceiptNumber() {
        String receiptNumber = PaymentService.generateReceiptNumber(2026L, 7L);
        assertEquals("RCPT-2026-0007", receiptNumber);
    }

    @Test
    void shouldIdentifyPaidAmountAsSettled() {
        assertTrue(PaymentService.isFullyPaid(new BigDecimal("5000.00"), new BigDecimal("5000.00")));
    }

    @Test
    void shouldCalculateRemainingInvoiceBalance() {
        assertEquals(new BigDecimal("350.00"), PaymentService.calculateRemainingBalance(
                new BigDecimal("1200.00"),
                new BigDecimal("850.00")
        ));
    }

    @Test
    void shouldDetermineBalanceStatus() {
        assertEquals("UNPAID", PaymentService.getBalanceStatus(new BigDecimal("1200.00"), BigDecimal.ZERO));
        assertEquals("PARTIALLY_PAID", PaymentService.getBalanceStatus(new BigDecimal("1200.00"), new BigDecimal("850.00")));
        assertEquals("PAID", PaymentService.getBalanceStatus(new BigDecimal("1200.00"), new BigDecimal("1200.00")));
    }

    @Test
    void shouldReturnInvoicesForSpecificPatient() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService service = new PaymentService(paymentRepository, invoiceRepository);

        Invoice invoice = new Invoice();
        invoice.setPatientName("Alice Johnson");
        invoice.setAmount(new BigDecimal("1500.00"));
        when(invoiceRepository.findByPatientNameIgnoreCaseOrderByCreatedAtDesc("Alice Johnson"))
                .thenReturn(List.of(invoice));

        List<Invoice> invoices = service.findInvoicesByPatientName("Alice Johnson");
        assertEquals(1, invoices.size());
        assertEquals("Alice Johnson", invoices.get(0).getPatientName());
    }
}
