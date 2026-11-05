package com.hospital.management;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.hospital.management.controller.HomeController;
import com.hospital.management.entity.User;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.service.PatientService;
import com.hospital.management.service.ReportService;
import com.hospital.management.service.UserService;

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
    void adminPatientsPageLoads() throws Exception {
        User admin = new User("System Administrator", "admin@hospital.com", "admin123", "ADMIN");

        mockMvc.perform(get("/admin/patients").sessionAttr("loggedInUser", admin))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-patients"));
    }

    @Test
    void userReportsPageLoads() throws Exception {
        User user = new User("Test User", "test@example.com", "user123", "USER");

        mockMvc.perform(get("/user/reports").sessionAttr("loggedInUser", user))
                .andExpect(status().isOk())
                .andExpect(view().name("user-reports"));
    }
}
