package com.hospital.management;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.hospital.management.service.UserService;
import com.hospital.management.service.DoctorService;

@SpringBootApplication
public class HospitalManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(HospitalManagementSystemApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner seedData(UserService userService, DoctorService doctorService) {
        return args -> {
            userService.createAdminIfNotExists();
            doctorService.seedDefaults();
        };
    }
}
