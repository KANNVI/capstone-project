package com.bloodconnect.config;

import com.bloodconnect.model.Donor;
import com.bloodconnect.model.Role;
import com.bloodconnect.model.User;
import com.bloodconnect.repository.DonorRepository;
import com.bloodconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * First start only: creates the admin account and (optionally) demo donors around Mohali/Chandigarh
 * so the matching engine can be demonstrated immediately.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final DonorRepository donors;
    private final PasswordEncoder encoder;

    @Value("${bloodconnect.seed-demo:false}")
    private boolean seedDemo;

    @Value("${bloodconnect.admin.email:admin@bloodconnect.com}")
    private String adminEmail;

    @Value("${bloodconnect.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        User admin = newUser("Admin", adminEmail, adminPassword, "9000000000", Role.ADMIN, "Mohali");
        admin.setVerified(true);
        users.save(admin);

        if (!seedDemo) return;

        newDonor("Amit Sharma",   "amit@demo.com",   "O+",  "Kharar",     30.7460, 76.6469, null);
        newDonor("Simran Kaur",   "simran@demo.com", "O+",  "Mohali",     30.7046, 76.7179, null);
        newDonor("Karan Singh",   "karan@demo.com",  "B+",  "Chandigarh", 30.7415, 76.7681, null);
        newDonor("Neha Verma",    "neha@demo.com",   "B-",  "Zirakpur",   30.6425, 76.8173, null);
        newDonor("Rohit Gill",    "rohit@demo.com",  "A+",  "Morinda",    30.7900, 76.4950, null);
        newDonor("Pooja Bansal",  "pooja@demo.com",  "O-",  "Panchkula",  30.6942, 76.8606, null);
        newDonor("Harpreet Dhillon", "harpreet@demo.com", "AB+", "Mohali", 30.7100, 76.7100, null);
        // donated 20 days ago -> NOT eligible, matching engine must skip this donor
        newDonor("Vikas Rest",    "vikas@demo.com",  "B+",  "Kharar",     30.7500, 76.6500, LocalDate.now().minusDays(20));

        User req = newUser("Rahul Mehta", "rahul@demo.com", "password123", "9876543210", Role.REQUESTER, "Mohali");
        users.save(req);
    }

    private User newUser(String name, String email, String pass, String phone, Role role, String city) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(pass));
        u.setPhone(phone);
        u.setRole(role);
        u.setCity(city);
        return u;
    }

    private void newDonor(String name, String email, String group, String city, double lat, double lng, LocalDate last) {
        User u = newUser(name, email, "password123", "98" + (10000000 + Math.abs(email.hashCode()) % 89999999), Role.DONOR, city);
        users.save(u);
        Donor d = new Donor();
        d.setUser(u);
        d.setBloodGroup(group);
        d.setLatitude(lat);
        d.setLongitude(lng);
        d.setLastDonationDate(last);
        d.setAvailable(true);
        donors.save(d);
    }
}
