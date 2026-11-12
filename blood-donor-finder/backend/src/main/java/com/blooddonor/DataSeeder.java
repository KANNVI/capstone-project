package com.blooddonor;

import com.blooddonor.model.*;
import com.blooddonor.repo.*;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Loads demo data on startup (H2 is in-memory). Admin login: admin@blood.com / admin123 */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users; private final DonorRepository donors;
    private final BankRepository banks; private final RequestRepository requests; private final PasswordEncoder enc;

    public DataSeeder(UserRepository u, DonorRepository d, BankRepository b, RequestRepository r, PasswordEncoder e) {
        users = u; donors = d; banks = b; requests = r; enc = e;
    }

    @Override public void run(String... args) {
        if (users.findByEmail("admin@blood.com").isPresent()) return;
        AppUser admin = new AppUser();
        admin.name = "Admin"; admin.email = "admin@blood.com"; admin.password = enc.encode("admin123"); admin.role = "ADMIN";
        users.save(admin);

        donor("Aarav Sharma", "O-", 28, 72, "9810011111", "Gurugram", 150, true, true);
        donor("Priya Singh", "A+", 24, 55, "9810022222", "Delhi", 200, true, true);
        donor("Rohan Verma", "B+", 31, 80, "9810033333", "Noida", 30, true, true);
        donor("Sneha Gupta", "O+", 26, 58, "9810044444", "Gurugram", 120, true, true);
        donor("Karan Mehta", "AB+", 35, 78, "9810055555", "Faridabad", 400, true, false);
        donor("Neha Kapoor", "A-", 29, 60, "9810066666", "Delhi", 95, false, true);
        donor("Vikram Rao", "B-", 40, 85, "9810077777", "Ghaziabad", 300, true, true);

        bank("Red Cross Blood Bank", "Delhi", "011-23711551", 4, 18, 3, 12, 2, 15, 1, 6);
        bank("Sector 10 Hospital Blood Centre", "Gurugram", "0124-2345678", 1, 9, 0, 7, 2, 8, 0, 3);
        bank("Noida District Blood Bank", "Noida", "0120-2456789", 2, 11, 1, 5, 0, 10, 1, 2);

        BloodRequest r = new BloodRequest();
        r.patient = "Ramesh Kumar"; r.bloodGroup = "A+"; r.units = 2; r.hospital = "City Care Hospital";
        r.city = "Delhi"; r.urgency = "Critical"; r.ownerEmail = "admin@blood.com"; r.verified = true;
        requests.save(r);
    }

    private void donor(String name, String g, int age, double w, String ph, String city, int daysAgo, boolean avail, boolean verified) {
        Donor d = new Donor();
        d.name = name; d.bloodGroup = g; d.age = age; d.weight = w; d.phone = ph; d.city = city;
        d.lastDonation = LocalDate.now().minusDays(daysAgo); d.available = avail; d.verified = verified;
        donors.save(d);
    }

    private void bank(String name, String city, String phone, int... s) {
        BloodBank b = new BloodBank();
        b.name = name; b.city = city; b.phone = phone;
        String[] g = {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"};
        for (int i = 0; i < g.length; i++) b.stock.put(g[i], s[i]);
        banks.save(b);
    }
}
