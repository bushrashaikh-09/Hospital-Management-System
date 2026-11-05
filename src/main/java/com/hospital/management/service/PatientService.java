package com.hospital.management.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hospital.management.entity.Patient;
import com.hospital.management.entity.User;
import com.hospital.management.repository.PatientRepository;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> findAll() { return patientRepository.findAll(); }
    public Optional<Patient> findById(Long id) { return patientRepository.findById(id); }
    public Optional<Patient> findByUserId(Long userId) { return patientRepository.findByUserId(userId); }

    public Patient createForUser(User user) {
        Patient patient = new Patient();
        patient.setUserId(user.getId());
        patient.setName(user.getFullName());
        patient.setStatus("Active");
        return save(patient);
    }

    public Patient save(Patient patient) {
        if (patient == null || patient.getName() == null || patient.getName().isBlank()) {
            throw new IllegalArgumentException("Patient name is required");
        }
        patient.setName(patient.getName().trim());
        if (patient.getStatus() == null || patient.getStatus().isBlank()) patient.setStatus("Active");
        return patientRepository.save(patient);
    }

    public void deleteById(Long id) {
        patientRepository.deleteById(id);
    }
}