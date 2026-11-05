package com.hospital.management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.management.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByOrderByCreatedAtDesc();
    List<Payment> findByPatientNameIgnoreCaseOrderByCreatedAtDesc(String patientName);
    List<Payment> findByInvoiceNumberOrderByCreatedAtDesc(String invoiceNumber);
    boolean existsByReceiptNumber(String receiptNumber);
}
