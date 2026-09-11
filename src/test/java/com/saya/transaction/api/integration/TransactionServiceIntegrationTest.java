package com.saya.transaction.api.integration;

import com.saya.transaction.api.dto.TransactionDto;
import com.saya.transaction.api.dto.TransferDto;
import com.saya.transaction.api.entity.Account;
import com.saya.transaction.api.entity.Customer;
import com.saya.transaction.api.entity.Transaction;
import com.saya.transaction.api.exception.AccountException;
import com.saya.transaction.api.exception.InsufficientBalanceException;
import com.saya.transaction.api.repository.AccountRepository;
import com.saya.transaction.api.repository.CustomerRepository;
import com.saya.transaction.api.repository.TransactionRepository;
import com.saya.transaction.api.service.TransactionService;
import com.saya.transaction.api.utils.TransactionConstants;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        transactionRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
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
    @Transactional
    void transfer_shouldKeepBalancesUnchanged_whenDestinationAccountIsClosed() {

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

    @Test
    void transfer_shouldNotChangeBalances_whenBalanceIsInsufficient() {
        Customer customer = new Customer();
        customer.setName("Test1");
        customer.setEmail("test@gmail.com");
        customer = customerRepository.save(customer);
        Account toAccount = new Account();

        toAccount.setCustomer(customer);
        toAccount.setAccountNumber("ACC001");
        toAccount.setAccountType(TransactionConstants.AccountType.SAVINGS);
        toAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount = accountRepository.save(toAccount);

        Account fromAccount = new Account();
        fromAccount.setCustomer(customer);
        fromAccount.setAccountNumber("ACC002");
        fromAccount.setAccountType(TransactionConstants.AccountType.SAVINGS);
        fromAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount = accountRepository.save(fromAccount);

        TransferDto request = new TransferDto();
        request.setFromAccount(fromAccount.getId());
        request.setToAccount(toAccount.getId());
        request.setAmount(new BigDecimal("1500.00"));

        assertThrows(
                InsufficientBalanceException.class,
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

    @Test
    void version_shouldIncrementAfterBalanceUpdate() {
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

        Long initVersion = account.getVersion();

        TransactionDto request = new TransactionDto();
        request.setAccountId(account.getId());
        request.setAmount(new BigDecimal("200.00"));
        request.setType(TransactionConstants.TransactionType.DEPOSIT);

        transactionService.depositTransaction(request);

        Account updated = accountRepository
                .findById(account.getId())
                .orElseThrow();

        assertTrue(updated.getVersion() > initVersion);

    }

    @Test
    @Transactional
    void transactionRecord_shouldBeCreatedAfterDeposit(){
        Customer customer = new Customer();
        customer.setName("Saya");
        customer.setEmail("saya@test.com");
        customer = customerRepository.save(customer);

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber("ACC012");
        account.setAccountNickname("Main");
        account.setAccountType(TransactionConstants.AccountType.SAVINGS);
        account.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("6000.00"));
        account = accountRepository.save(account);


        TransactionDto request = new TransactionDto();
        request.setAccountId(account.getId());
        request.setAmount(new BigDecimal("200.00"));
        request.setType(TransactionConstants.TransactionType.DEPOSIT);
        transactionService.depositTransaction(request);

        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
        assertEquals(TransactionConstants.TransactionType.DEPOSIT, transactions.get(0).getType());
        assertEquals(
                0,
                new BigDecimal("200.00").compareTo(transactions.get(0).getAmount()));

        assertEquals(account.getAccountNumber(), transactions.get(0).getAccount().getAccountNumber());
        assertEquals(account.getAccountNickname(), transactions.get(0).getAccount().getAccountNickname());

    }

    @Test
    @Transactional
    void transfer_shouldSuccess() {

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
        toAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount = accountRepository.save(toAccount);

        TransferDto request = new TransferDto();
        request.setFromAccount(fromAccount.getId());
        request.setToAccount(toAccount.getId());
        request.setAmount(new BigDecimal("100.00"));

        transactionService.transferTransaction(request);

        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(fromAccount.getId());

        assertEquals(1, transactions.size());
        assertEquals(transactions.get(0).getAmount(), request.getAmount());
        assertEquals(toAccount.getId(),transactions.get(0).getToAccountId());
        assertEquals(TransactionConstants.TransactionType.TRANSFER, transactions.get(0).getType());
        assertEquals(request.getFromAccount(),transactions.get(0).getAccount().getId());


        Account fromAfter = accountRepository
                .findById(fromAccount.getId())
                .orElseThrow();

        Account toAfter = accountRepository
                .findById(toAccount.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("900.00").compareTo(fromAfter.getBalance())
        );

        assertEquals(
                0,
                new BigDecimal("600.00").compareTo(toAfter.getBalance())
        );
    }


}
