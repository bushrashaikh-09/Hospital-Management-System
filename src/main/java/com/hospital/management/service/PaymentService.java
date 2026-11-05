package com.hospital.management.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import com.hospital.management.entity.Invoice;
import com.hospital.management.entity.Payment;
import com.hospital.management.repository.InvoiceRepository;
import com.hospital.management.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<Payment> findAll() {
        return paymentRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Invoice> findInvoicesByPatientName(String patientName) {
        if (patientName == null || patientName.isBlank()) {
            return List.of();
        }
        return invoiceRepository.findByPatientNameIgnoreCaseOrderByCreatedAtDesc(patientName.trim());
    }

    public List<Payment> findPaymentsByPatientName(String patientName) {
        if (patientName == null || patientName.isBlank()) {
            return List.of();
        }
        return paymentRepository.findByPatientNameIgnoreCaseOrderByCreatedAtDesc(patientName.trim());
    }

    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<PatientBalanceSummary> getPatientBalanceSummary() {
        Map<String, BigDecimal> totalInvoiceValueByPatient = new HashMap<>();
        for (Invoice invoice : invoiceRepository.findAllByOrderByCreatedAtDesc()) {
            if (invoice.getPatientName() == null || invoice.getPatientName().isBlank()) {
                continue;
            }
            String patientName = invoice.getPatientName().trim();
            BigDecimal invoiceAmount = invoice.getAmount() == null ? BigDecimal.ZERO : invoice.getAmount();
            totalInvoiceValueByPatient.merge(patientName, invoiceAmount, BigDecimal::add);
        }

        Map<String, BigDecimal> totalPaidByPatient = new HashMap<>();
        for (Payment payment : paymentRepository.findAllByOrderByCreatedAtDesc()) {
            String patientName = payment.getPatientName();
            if (patientName == null || patientName.isBlank()) {
                if (payment.getInvoiceNumber() != null && !payment.getInvoiceNumber().isBlank()) {
                    Optional<Invoice> invoice = invoiceRepository.findByInvoiceNumber(payment.getInvoiceNumber());
                    if (invoice.isPresent() && invoice.get().getPatientName() != null) {
                        patientName = invoice.get().getPatientName();
                    }
                }
            }
            if (patientName == null || patientName.isBlank()) {
                continue;
            }
            BigDecimal amount = payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount();
            totalPaidByPatient.merge(patientName.trim(), amount, BigDecimal::add);
        }

        Set<String> patientNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        patientNames.addAll(totalInvoiceValueByPatient.keySet());
        patientNames.addAll(totalPaidByPatient.keySet());

        List<PatientBalanceSummary> summary = new ArrayList<>();
        for (String patientName : patientNames) {
            BigDecimal totalInvoices = totalInvoiceValueByPatient.getOrDefault(patientName, BigDecimal.ZERO);
            BigDecimal totalPaid = totalPaidByPatient.getOrDefault(patientName, BigDecimal.ZERO);
            BigDecimal outstanding = calculateRemainingBalance(totalInvoices, totalPaid);
            summary.add(new PatientBalanceSummary(
                    patientName,
                    totalInvoices,
                    totalPaid,
                    outstanding,
                    getBalanceStatus(totalInvoices, totalPaid)
            ));
        }

        summary.sort(Comparator.comparing(PatientBalanceSummary::getOutstandingAmount).reversed());
        return summary;
    }

    public Payment save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment is required");
        }
        if (payment.getPatientName() == null || payment.getPatientName().isBlank()) {
            throw new IllegalArgumentException("Patient name is required");
        }
        if (payment.getAmount() == null) {
            payment.setAmount(BigDecimal.ZERO);
        }
        if (payment.getStatus() == null || payment.getStatus().isBlank()) {
            payment.setStatus("Completed");
        }
        if (payment.getReceiptNumber() == null || payment.getReceiptNumber().isBlank()) {
            payment.setReceiptNumber(generateReceiptNumber(
                    java.time.LocalDate.now().getYear(),
                    paymentRepository.count() + 1
            ));
        }

        Payment saved = paymentRepository.save(payment);
        syncInvoiceStatus(saved.getInvoiceNumber());
        return saved;
    }

    public void deleteById(Long id) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment != null) {
            syncInvoiceStatus(payment.getInvoiceNumber());
        }
        paymentRepository.deleteById(id);
    }

    public void syncInvoiceStatus(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            return;
        }

        Optional<Invoice> invoiceOpt = invoiceRepository.findByInvoiceNumber(invoiceNumber);
        if (invoiceOpt.isEmpty()) {
            return;
        }

        Invoice invoice = invoiceOpt.get();
        BigDecimal totalPaid = paymentRepository.findByInvoiceNumberOrderByCreatedAtDesc(invoiceNumber)
                .stream()
                .map(Payment::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = calculateRemainingBalance(invoice.getAmount(), totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus("Paid");
        } else if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(LocalDate.now())
                && !"Paid".equalsIgnoreCase(invoice.getStatus())
                && !"Cancelled".equalsIgnoreCase(invoice.getStatus())) {
            invoice.setStatus("Overdue");
        } else {
            invoice.setStatus("Pending");
        }

        invoiceRepository.save(invoice);
    }

    public static String generateReceiptNumber(Long year, Long sequence) {
        return generateReceiptNumber(year.longValue(), sequence.longValue());
    }

    public static String generateReceiptNumber(long year, long sequence) {
        return String.format("RCPT-%d-%04d", year, sequence);
    }

    public static BigDecimal calculateRemainingBalance(BigDecimal invoiceAmount, BigDecimal totalPaid) {
        if (invoiceAmount == null) {
            return BigDecimal.ZERO;
        }
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }
        BigDecimal remaining = invoiceAmount.subtract(totalPaid);
        return remaining.max(BigDecimal.ZERO);
    }

    public static String getBalanceStatus(BigDecimal totalAmount, BigDecimal paidAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "PAID";
        }
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (paidAmount.compareTo(totalAmount) >= 0) {
            return "PAID";
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "UNPAID";
        }
        return "PARTIALLY_PAID";
    }

    public static boolean isFullyPaid(BigDecimal received, BigDecimal total) {
        if (received == null || total == null) {
            return false;
        }
        return received.compareTo(total) >= 0;
    }

    public static class PatientBalanceSummary {
        private final String patientName;
        private final BigDecimal totalInvoices;
        private final BigDecimal totalPaid;
        private final BigDecimal outstandingAmount;
        private final String status;

        public PatientBalanceSummary(String patientName, BigDecimal totalInvoices, BigDecimal totalPaid,
                                    BigDecimal outstandingAmount, String status) {
            this.patientName = patientName;
            this.totalInvoices = totalInvoices == null ? BigDecimal.ZERO : totalInvoices;
            this.totalPaid = totalPaid == null ? BigDecimal.ZERO : totalPaid;
            this.outstandingAmount = outstandingAmount == null ? BigDecimal.ZERO : outstandingAmount;
            this.status = status;
        }

        public String getPatientName() {
            return patientName;
        }

        public BigDecimal getTotalInvoices() {
            return totalInvoices;
        }

        public BigDecimal getTotalPaid() {
            return totalPaid;
        }

        public BigDecimal getOutstandingAmount() {
            return outstandingAmount;
        }

        public String getStatus() {
            return status;
        }
    }
}
