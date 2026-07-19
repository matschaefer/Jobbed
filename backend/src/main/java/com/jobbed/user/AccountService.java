package com.jobbed.user;

import com.jobbed.application.JobApplicationRepository;
import com.jobbed.common.error.exception.InvalidCredentialsException;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.company.CompanyRepository;
import com.jobbed.document.DocumentRepository;
import com.jobbed.document.FileStorageService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final UserRepository users;
    private final DocumentRepository documents;
    private final JobApplicationRepository applications;
    private final CompanyRepository companies;
    private final FileStorageService fileStorage;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository users,
                          DocumentRepository documents,
                          JobApplicationRepository applications,
                          CompanyRepository companies,
                          FileStorageService fileStorage,
                          PasswordEncoder passwordEncoder) {
        this.users = users;
        this.documents = documents;
        this.applications = applications;
        this.companies = companies;
        this.fileStorage = fileStorage;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void deleteAccount(UUID userId, String password) {
        User user = users.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Nutzer", userId));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        documents.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(document -> fileStorage.delete(document.getStoragePath()));
        documents.deleteByUserId(userId);
        applications.deleteByUserId(userId);
        companies.deleteByUserId(userId);
        users.delete(user);
    }
}
