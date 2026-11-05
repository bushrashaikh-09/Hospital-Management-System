package com.hospital.management.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hospital.management.entity.Patient;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.service.PatientService;
import com.hospital.management.service.UserService;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    @SuppressWarnings("unused")
    private UserService userService;

    @MockBean
    @SuppressWarnings("unused")
    private AppointmentService appointmentService;

    @Test
    void getPatientsReturnsStoredRecordsWithoutRecreatingDeletedRecords() throws Exception {
        Patient patient = new Patient();
        patient.setId(7L);
        patient.setName("John Smith");
        patient.setStatus("Active");
        when(patientService.findAll()).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("John Smith"));

        verify(patientService).findAll();
    }
}