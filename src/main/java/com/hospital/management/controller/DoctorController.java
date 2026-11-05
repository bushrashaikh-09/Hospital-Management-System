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

import com.hospital.management.entity.Doctor;
import com.hospital.management.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<Doctor> getDoctors() {
        return doctorService.findAll();
    }

    @PostMapping
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return doctorService.save(doctor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        Doctor existing = doctorService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        existing.setName(doctor.getName());
        existing.setSpecialty(doctor.getSpecialty());
        existing.setServices(doctor.getServices());
        existing.setExperienceYears(doctor.getExperienceYears());
        existing.setAvailable(doctor.isAvailable());
        return ResponseEntity.ok(doctorService.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        Doctor existing = doctorService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        doctorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}