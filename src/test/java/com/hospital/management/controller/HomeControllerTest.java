package com.hospital.management.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.hospital.management.entity.Appointment;
import com.hospital.management.entity.User;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.service.PatientService;
import com.hospital.management.service.ReportService;
import com.hospital.management.service.UserService;

@SuppressWarnings("unused")
@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private ReportService reportService;

    @Test
    void userDoctorsPageShouldLoadForLoggedInUser() throws Exception {
        User user = new User();
        user.setFullName("Alice Johnson");
        user.setEmail("alice@example.com");
        user.setPassword("secret123");
        user.setRole("USER");

        mockMvc.perform(get("/user/doctors")
                        .sessionAttr("loggedInUser", user))
                .andExpect(status().isOk())
                .andExpect(view().name("user-doctors"));
    }

    @Test
    void userDashboardCountsOnlyVisitsThatAreNotCompleted() throws Exception {
        User user = new User();
        user.setFullName("Alice Johnson");
        user.setRole("USER");

        Appointment completed = new Appointment();
        completed.setPatientName(user.getFullName());
        completed.setStatus("Completed");
        Appointment pending = new Appointment();
        pending.setPatientName(user.getFullName());
        pending.setStatus("Pending");
        when(appointmentService.findAll()).thenReturn(List.of(completed, pending));
        when(reportService.findSentReportsByPatientName(user.getFullName())).thenReturn(List.of());

        mockMvc.perform(get("/user/dashboard").sessionAttr("loggedInUser", user))
                .andExpect(status().isOk())
                .andExpect(view().name("user-dashboard"))
                .andExpect(model().attribute("upcomingVisits", 1L));
    }
}
