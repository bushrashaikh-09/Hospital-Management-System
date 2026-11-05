package com.hospital.management.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hospital.management.entity.Appointment;
import com.hospital.management.entity.Report;
import com.hospital.management.entity.User;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.service.PatientService;
import com.hospital.management.service.ReportService;
import com.hospital.management.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

    @Value("${app.payment.upi-id:hospital@upi}")
    private String paymentUpiId;

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final ReportService reportService;

    public HomeController(UserService userService, AppointmentService appointmentService, PatientService patientService, ReportService reportService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.reportService = reportService;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            User user = (User) session.getAttribute("loggedInUser");
            return redirectByRole(user.getRole());
        }
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Optional<User> userOptional = userService.findByEmail(email.trim());

        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Invalid email or password");
            model.addAttribute("user", new User());
            return "login";
        }

        User user = userOptional.get();
        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid email or password");
            model.addAttribute("user", new User());
            return "login";
        }

        session.setAttribute("loggedInUser", user);
        return redirectByRole(user.getRole());
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            User registeredUser = userService.registerUser(user);
            patientService.createForUser(registeredUser);
            model.addAttribute("success", "Registration successful! Please login.");
            model.addAttribute("user", new User());
            return "login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        long totalPatients = patientService.findAll().size();
        long staffMembers = userService.countByRole("ADMIN");
        List<Appointment> allAppointments = appointmentService.findAll();
        List<Report> allReports = reportService.findAll();
        long totalAppointments = allAppointments.size();
        long totalReports = allReports.size();
        long completedAppointments = allAppointments.stream()
            .filter(appointment -> "Completed".equalsIgnoreCase(appointment.getStatus()))
            .count();
        long activeAppointments = allAppointments.stream()
            .filter(appointment -> !"Cancelled".equalsIgnoreCase(appointment.getStatus()))
            .count();
        long reviewReports = allReports.stream()
            .filter(report -> !"Approved".equalsIgnoreCase(report.getStatus())
                && !"Reviewed".equalsIgnoreCase(report.getStatus()))
            .count();
        List<User> recentUsers = userService.findRecentUsers();

        model.addAttribute("user", user);
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("appointments", totalAppointments);
        model.addAttribute("staffMembers", staffMembers);
        model.addAttribute("newCases", totalReports);
        model.addAttribute("patientCheckIns", percentage(completedAppointments, activeAppointments));
        model.addAttribute("careSatisfaction", percentage(
            allReports.stream().filter(report -> "Approved".equalsIgnoreCase(report.getStatus())).count(), totalReports));
        model.addAttribute("dischargeRate", percentage(completedAppointments, totalAppointments));
        model.addAttribute("pendingReview", percentage(reviewReports, totalReports));
        model.addAttribute("recentUsers", recentUsers);
        return "admin-dashboard";
    }

    @GetMapping("/user/dashboard")
    public String userDashboard(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }

        String fullName = user.getFullName();
        List<Appointment> appointments = (fullName != null && !fullName.isBlank()) ?
                appointmentService.findAll().stream()
                    .filter(appointment -> fullName.equalsIgnoreCase(appointment.getPatientName()))
                    .filter(appointment -> !"Cancelled".equalsIgnoreCase(appointment.getStatus()))
                    .toList() :
                List.of();
        
        List<Report> reports = (fullName != null && !fullName.isBlank()) ?
                reportService.findSentReportsByPatientName(fullName) :
                List.of();

        model.addAttribute("user", user);
        model.addAttribute("appointments", appointments);
        model.addAttribute("reports", reports);
        model.addAttribute("upcomingVisits", appointments.stream()
            .filter(appointment -> !"Completed".equalsIgnoreCase(appointment.getStatus()))
            .count());
        model.addAttribute("reportCount", reports.size());
        return "user-dashboard";
    }

    @GetMapping("/user/billing")
    public String userBilling(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("upiId", paymentUpiId);
        return "user-billing";
    }

    @GetMapping("/user/profile")
    public String userProfile(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "user-profile";
    }

    @GetMapping("/admin/patients")
    public String adminPatients(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin-patients";
    }

    @GetMapping("/admin/appointments")
    public String adminAppointments(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin-appointments";
    }

    @GetMapping("/admin/reports")
    public String adminReports(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        List<Appointment> allAppointments = appointmentService.findAll();
        List<Report> allReports = reportService.findAll();
        long completedAppointments = allAppointments.stream()
            .filter(appointment -> "Completed".equalsIgnoreCase(appointment.getStatus()))
            .count();
        long activeAppointments = allAppointments.stream()
            .filter(appointment -> !"Cancelled".equalsIgnoreCase(appointment.getStatus()))
            .count();
        model.addAttribute("patientCheckIns", percentage(completedAppointments, activeAppointments));
        model.addAttribute("careSatisfaction", percentage(
            allReports.stream().filter(report -> "Approved".equalsIgnoreCase(report.getStatus())).count(), allReports.size()));
        model.addAttribute("dischargeRate", percentage(completedAppointments, allAppointments.size()));
        model.addAttribute("pendingReview", percentage(
            allReports.stream().filter(report -> !"Approved".equalsIgnoreCase(report.getStatus())
                && !"Reviewed".equalsIgnoreCase(report.getStatus())).count(), allReports.size()));
        model.addAttribute("user", user);
        return "admin-reports";
    }

    @GetMapping("/admin/doctors")
    public String adminDoctors(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin-doctors";
    }

    @GetMapping("/admin/settings")
    public String adminSettings(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin-settings";
    }

    @GetMapping("/admin/invoices")
    public String adminInvoices(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin-invoices";
    }

    @GetMapping("/admin/payments")
    public String adminPayments(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin-payments";
    }

    @GetMapping("/user/doctors")
    public String userDoctors(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "user-doctors";
    }

    @GetMapping("/user/appointments")
    public String userAppointments(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "user-appointments";
    }

    @GetMapping("/user/reports")
    public String userReports(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "user-reports";
    }

    @GetMapping("/user/settings")
    public String userSettings(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "user-settings";
    }

    @PostMapping("/user/profile")
    public String updateProfile(@RequestParam String fullName,
                               @RequestParam String email,
                               @RequestParam(defaultValue = "") String phoneNumber,
                               @RequestParam(defaultValue = "") String address,
                               HttpSession session,
                               Model model) {
        User currentUser = getLoggedInUser(session);
        if (currentUser == null || !"USER".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        // Validate inputs
        if (fullName == null || fullName.trim().isEmpty()) {
            model.addAttribute("error", "Full name cannot be empty");
            model.addAttribute("user", currentUser);
            return "user-profile";
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            model.addAttribute("error", "Please enter a valid email");
            model.addAttribute("user", currentUser);
            return "user-profile";
        }

        // Check if email is already taken by another user
        String newEmail = email.trim().toLowerCase();
        if (!currentUser.getEmail().equals(newEmail)) {
            Optional<User> existingUser = userService.findByEmail(newEmail);
            if (existingUser.isPresent()) {
                model.addAttribute("error", "Email already exists");
                model.addAttribute("user", currentUser);
                return "user-profile";
            }
        }

        currentUser.setFullName(fullName.trim());
        currentUser.setEmail(newEmail);
        currentUser.setPhoneNumber(phoneNumber == null ? "" : phoneNumber.trim());
        currentUser.setAddress(address == null ? "" : address.trim());
        userService.save(currentUser);
        session.setAttribute("loggedInUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("success", "Profile updated successfully");
        return "user-profile";
    }

    private User getLoggedInUser(HttpSession session) {
        Object obj = session.getAttribute("loggedInUser");
        return obj instanceof User user ? user : null;
    }

    private String redirectByRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/user/dashboard";
    }

    private int percentage(long numerator, long denominator) {
        return denominator == 0 ? 0 : (int) Math.round((numerator * 100.0) / denominator);
    }
}
