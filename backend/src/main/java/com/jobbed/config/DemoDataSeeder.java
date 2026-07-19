package com.jobbed.config;

import com.jobbed.application.ApplicationStatus;
import com.jobbed.application.JobApplication;
import com.jobbed.application.JobApplicationRepository;
import com.jobbed.application.Priority;
import com.jobbed.application.WorkModel;
import com.jobbed.company.Company;
import com.jobbed.company.CompanyRepository;
import com.jobbed.user.Role;
import com.jobbed.user.User;
import com.jobbed.user.UserProfile;
import com.jobbed.user.UserProfileRepository;
import com.jobbed.user.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {
    public static final String EMAIL = "analytics@jobbed.local";
    public static final String PASSWORD = "Str0ng!Passw0rd";
    public static final String SECURITY_EMAIL = "e2e-security@jobbed.local";

    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final CompanyRepository companies;
    private final JobApplicationRepository applications;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository users, UserProfileRepository profiles, CompanyRepository companies,
            JobApplicationRepository applications, PasswordEncoder passwordEncoder) {
        this.users = users; this.profiles = profiles; this.companies = companies;
        this.applications = applications; this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!users.existsByEmail(EMAIL)) seedMainDemo();
        if (!users.existsByEmail(SECURITY_EMAIL)) seedSecurityDemo();
    }

    private void seedMainDemo() {
        User user = createUser("Ana", "Lytics", EMAIL);

        Company acme = company(user, "Acme", "Berlin", "Software");
        Company techCorp = company(user, "TechCorp", "Hamburg", "Technologie");
        Company globex = company(user, "Globex", "München", "Digital Services");
        companies.saveAll(List.of(acme, techCorp, globex));

        create(user, acme, "Product Manager", ApplicationStatus.SAVED, Priority.HIGH, 3);
        create(user, techCorp, "Frontend Developer", ApplicationStatus.APPLIED, Priority.HIGH, 8);
        create(user, globex, "Data Analyst", ApplicationStatus.SCREENING, Priority.MEDIUM, 14);
        create(user, techCorp, "Backend Engineer", ApplicationStatus.INTERVIEW, Priority.URGENT, 20);
        create(user, globex, "DevOps Engineer", ApplicationStatus.TECHNICAL_INTERVIEW, Priority.HIGH, 27);
        create(user, acme, "QA Engineer", ApplicationStatus.OFFER, Priority.MEDIUM, 35);
        create(user, techCorp, "Full Stack Dev", ApplicationStatus.REJECTED, Priority.LOW, 43);
        create(user, globex, "UX Designer", ApplicationStatus.ARCHIVED, Priority.LOW, 52);
    }

    private void seedSecurityDemo() {
        User user = createUser("E2E", "Security", SECURITY_EMAIL);
        Company company = company(user, "Private Test Company", "Köln", "Security Test");
        companies.save(company);
        create(user, company, "Private Test Application", ApplicationStatus.SAVED, Priority.LOW, 1);
    }

    private User createUser(String firstName, String lastName, String email) {
        User user = new User(); user.setFirstName(firstName); user.setLastName(lastName); user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD)); user.setRole(Role.USER); user.setEnabled(true); user.setEmailVerified(true);
        users.save(user); profiles.save(new UserProfile(user)); return user;
    }

    private Company company(User user, String name, String location, String industry) {
        Company value = new Company(); value.setUserId(user.getId()); value.setName(name);
        value.setLocation(location); value.setIndustry(industry); return value;
    }
    private void create(User user, Company company, String title, ApplicationStatus status, Priority priority, int daysAgo) {
        JobApplication value = new JobApplication(); value.setUserId(user.getId()); value.setCompany(company);
        value.setJobTitle(title); value.setCurrentStatus(status); value.setPriority(priority);
        value.setApplicationDate(LocalDate.now().minusDays(daysAgo)); value.setLocation(company.getLocation());
        value.setWorkModel(WorkModel.HYBRID); value.setSource("Demo-Daten"); applications.save(value);
    }
}
