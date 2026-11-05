package com.hospital.management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.management.entity.Invoice;
import com.hospital.management.service.InvoiceService;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/invoices")
    public List<Invoice> getInvoices() {
        return invoiceService.findAll();
    }

    @PostMapping("/invoices")
    public Invoice createInvoice(@RequestBody Invoice invoice) {
        return invoiceService.save(invoice);
    }

    @PutMapping("/invoices/{id}")
    public ResponseEntity<Invoice> updateInvoice(@PathVariable Long id, @RequestBody Invoice invoice) {
        return invoiceService.findById(id)
                .map(existing -> {
                    existing.setPatientName(invoice.getPatientName());
                    existing.setDoctorName(invoice.getDoctorName());
                    existing.setDepartment(invoice.getDepartment());
                    existing.setAppointmentDate(invoice.getAppointmentDate());
                    existing.setInvoiceDate(invoice.getInvoiceDate());
                    existing.setDueDate(invoice.getDueDate());
                    existing.setAmount(invoice.getAmount());
                    existing.setStatus(invoice.getStatus());
                    existing.setNotes(invoice.getNotes());
                    return ResponseEntity.ok(invoiceService.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/invoices/{id}")
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteById(id);
    }
}
