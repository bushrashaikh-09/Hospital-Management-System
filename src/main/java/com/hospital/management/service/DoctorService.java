package com.hospital.management.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.management.entity.Doctor;
import com.hospital.management.repository.DoctorRepository;

@Service
public class DoctorService {

    private static final String[][] DEFAULT_DOCTORS = {
        {"Dr. Rahul Mehta", "Cardiology", "ECG, Heart Consultation, Stress Test"},
        {"Dr. Priya Shah", "Neurology", "MRI Review, Neurology Checkup, Headache Care"},
        {"Dr. Ananya Nair", "Dermatology", "Skin Care, Acne Treatment, Allergy Testing"},
        {"Dr. Karan Singh", "Orthopedics", "Bone Scan, Joint Care, Physiotherapy"},
        {"Dr. Meera Joshi", "Pediatrics", "Child Checkup, Vaccination, Development Review"},
        {"Dr. Vikram Rao", "General Medicine", "General Consultation, Diabetes Care, Preventive Care"}
    };

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAllByOrderByNameAsc();
    }

    public Doctor save(Doctor doctor) {
        if (doctor == null || doctor.getName() == null || doctor.getName().isBlank()) {
            throw new IllegalArgumentException("Doctor name is required");
        }
        if (doctor.getSpecialty() == null || doctor.getSpecialty().isBlank()) {
            throw new IllegalArgumentException("Doctor specialty is required");
        }
        return doctorRepository.save(doctor);
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        doctorRepository.deleteById(id);
    }

    @Transactional
    public void seedDefaults() {
        if (doctorRepository.count() > 0) return;
        for (String[] values : DEFAULT_DOCTORS) {
            Doctor doctor = new Doctor();
            doctor.setName(values[0]);
            doctor.setSpecialty(values[1]);
            doctor.setServices(values[2]);
            doctorRepository.save(doctor);
        }
    }
}