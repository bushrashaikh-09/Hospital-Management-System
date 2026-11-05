package com.hospital.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.management.entity.Invoice;
import com.hospital.management.entity.Payment;
import com.hospital.management.entity.User;
import com.hospital.management.service.PaymentService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
public class UserBillingController {

    private final PaymentService paymentService;

    public UserBillingController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/invoices")
    public ResponseEntity<?> getUserInvoices(HttpSession session) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equalsIgnoreCase(user.getRole())) {
            return unauthorized();
        }

        List<Invoice> invoices = paymentService.findInvoicesByPatientName(user.getFullName());
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/payments")
    public ResponseEntity<?> getUserPayments(HttpSession session) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equalsIgnoreCase(user.getRole())) {
            return unauthorized();
        }

        List<Payment> payments = paymentService.findPaymentsByPatientName(user.getFullName());
        return ResponseEntity.ok(payments);
    }

    private User getLoggedInUser(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        return loggedInUser instanceof User user ? user : null;
    }

    private ResponseEntity<String> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User login required");
    }
}