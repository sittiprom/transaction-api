package com.saya.transaction.api.controller;

import com.saya.transaction.api.dto.AccountDto;
import com.saya.transaction.api.dto.TransactionDto;
import com.saya.transaction.api.dto.TransferDto;
import com.saya.transaction.api.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private  final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transaction/deposit")
    public AccountDto deposit(@Valid @RequestBody TransactionDto transactionDto) {
         return transactionService.depositTransaction(transactionDto);
    }


    @PostMapping("/transaction/withdrawal")
    public AccountDto withdrawal(@Valid @RequestBody TransactionDto transactionDto) {
        return transactionService.withdrawalTransaction(transactionDto);
    }


    @PostMapping("/transaction/transfer")
    public AccountDto transfer(@Valid @RequestBody TransferDto transferDto) {
        return transactionService.transferTransaction(transferDto);
    }
}
