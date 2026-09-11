package com.saya.transaction.api.integration;

import com.saya.transaction.api.dto.TransactionDto;
import com.saya.transaction.api.dto.TransferDto;
import com.saya.transaction.api.entity.Account;
import com.saya.transaction.api.entity.Customer;
import com.saya.transaction.api.exception.AccountException;
import com.saya.transaction.api.repository.AccountRepository;
import com.saya.transaction.api.repository.CustomerRepository;
import com.saya.transaction.api.repository.TransactionRepository;
import com.saya.transaction.api.service.TransactionService;
import com.saya.transaction.api.utils.TransactionConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TransactionServiceIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void deposit_shouldPersistBalanceUsingDirtyChecking() {

        Customer customer = new Customer();
        customer.setName("Saya");
        customer.setEmail("saya@test.com");
        customer = customerRepository.save(customer);

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber("ACC001");
        account.setAccountNickname("Main");
        account.setAccountType(TransactionConstants.AccountType.SAVINGS);
        account.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("1000.00"));
        account = accountRepository.save(account);

        TransactionDto request = new TransactionDto();
        request.setAccountId(account.getId());
        request.setAmount(new BigDecimal("200.00"));
        request.setType(TransactionConstants.TransactionType.DEPOSIT);

        transactionService.depositTransaction(request);

        Account updated = accountRepository
                .findById(account.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("1200.00")
                        .compareTo(updated.getBalance())
        );
    }

    @Test
    void transfer_shouldRollback_whenDestinationAccountIsClosed() {

        Customer customer = new Customer();
        customer.setName("Saya");
        customer.setEmail("saya@test.com");
        customer = customerRepository.save(customer);

        Account fromAccount = new Account();
        fromAccount.setCustomer(customer);
        fromAccount.setAccountNumber("ACC001");
        fromAccount.setAccountNickname("From");
        fromAccount.setAccountType(TransactionConstants.AccountType.SAVINGS);
        fromAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount = accountRepository.save(fromAccount);

        Account toAccount = new Account();
        toAccount.setCustomer(customer);
        toAccount.setAccountNumber("ACC002");
        toAccount.setAccountNickname("To");
        toAccount.setAccountType(TransactionConstants.AccountType.SAVINGS);
        toAccount.setStatus(TransactionConstants.AccountStatus.CLOSED);
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount = accountRepository.save(toAccount);

        TransferDto request = new TransferDto();
        request.setFromAccount(fromAccount.getId());
        request.setToAccount(toAccount.getId());
        request.setAmount(new BigDecimal("100.00"));

        assertThrows(
                AccountException.class,
                () -> transactionService.transferTransaction(request)
        );

        Account fromAfter = accountRepository
                .findById(fromAccount.getId())
                .orElseThrow();

        Account toAfter = accountRepository
                .findById(toAccount.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("1000.00").compareTo(fromAfter.getBalance())
        );

        assertEquals(
                0,
                new BigDecimal("500.00").compareTo(toAfter.getBalance())
        );
    }



}
