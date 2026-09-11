package com.saya.transaction.api.controller;

import com.saya.transaction.api.dto.AccountDto;
import com.saya.transaction.api.dto.CreateAccountRequest;
import com.saya.transaction.api.service.AccountService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/account/create")
    public AccountDto createAccount(@Valid  @RequestBody CreateAccountRequest request) {
        return  accountService.createAccount(request);

    }

    @GetMapping("/account/{id}")
    public AccountDto checkBalance(@PathVariable Long id) {
        return  accountService.getAccountDetail(id);

    }

}
