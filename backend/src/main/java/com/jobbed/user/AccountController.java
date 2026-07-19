package com.jobbed.user;

import com.jobbed.security.SecurityUtils;
import com.jobbed.user.dto.AccountDeletionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody AccountDeletionRequest request) {
        accountService.deleteAccount(SecurityUtils.currentUserId(), request.password());
        return ResponseEntity.noContent().build();
    }
}
