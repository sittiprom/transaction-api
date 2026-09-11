package com.saya.transaction.api.service;

import com.saya.transaction.api.dto.AccountDto;
import com.saya.transaction.api.dto.TransactionDto;
import com.saya.transaction.api.dto.TransferDto;
import com.saya.transaction.api.entity.Account;
import com.saya.transaction.api.entity.Transaction;
import com.saya.transaction.api.exception.AccountException;
import com.saya.transaction.api.exception.InsufficientBalanceException;
import com.saya.transaction.api.exception.InvalidTransactionException;
import com.saya.transaction.api.repository.AccountRepository;
import com.saya.transaction.api.utils.TransactionConstants;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.saya.transaction.api.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository, ModelMapper modelMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public AccountDto withdrawalTransaction(TransactionDto transactionDto) {
        if(!TransactionConstants.TransactionType.WITHDRAWAL.equals(transactionDto.getType())){
            throw new InvalidTransactionException("The Transaction type is not WITHDRAWAL ");
        }

        if (transactionDto.getAmount() == null ||
                transactionDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than 0");
        }

        Account account = accountRepository.findById(transactionDto.getAccountId())
                .orElseThrow(() -> new AccountException("Account not found"));

        if (account.getStatus() != TransactionConstants.AccountStatus.ACTIVE) {
            throw new AccountException("Account status must be ACTIVE");
        }


        Transaction transaction = new Transaction();
        transaction.setType(TransactionConstants.TransactionType.WITHDRAWAL);
        transaction.setAmount(transactionDto.getAmount());
        transaction.setAccount(account);
        if (account.getBalance().compareTo(transactionDto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient Balance");
        }
        account.setBalance(account.getBalance().subtract(transactionDto.getAmount()));
        transaction.setStatus(TransactionConstants.TransactionStatus.COMPLETED);
        transaction.setProcessedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return modelMapper.map(account, AccountDto.class);


    }

    @Transactional
    public AccountDto depositTransaction(TransactionDto transactionDto) {

        if(!TransactionConstants.TransactionType.DEPOSIT.equals(transactionDto.getType())){
            throw new InvalidTransactionException("The Transaction type is not DEPOSIT ");
        }


        if (transactionDto.getAmount() == null ||
                transactionDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than 0");
        }

        Account account = accountRepository.findById(transactionDto.getAccountId())
                .orElseThrow(() -> new AccountException("Account not found"));
        if (account.getStatus() != TransactionConstants.AccountStatus.ACTIVE) {
            throw new AccountException("Account status must be ACTIVE");
        }

        Transaction transaction = new Transaction();
        transaction.setType(TransactionConstants.TransactionType.DEPOSIT);
        transaction.setAmount(transactionDto.getAmount());
        transaction.setAccount(account);
        account.setBalance(account.getBalance().add(transactionDto.getAmount()));
        transaction.setStatus(TransactionConstants.TransactionStatus.COMPLETED);
        transaction.setProcessedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        return modelMapper.map(account, AccountDto.class);


    }

    @Transactional
    public AccountDto transferTransaction(TransferDto transferDto) {

        if (transferDto.getFromAccount().equals(transferDto.getToAccount())) {
            throw new InvalidTransactionException("From account and To account cannot be the same");
        }

        if (transferDto.getAmount() == null || transferDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than 0");
        }


        Account fromAccount = accountRepository.findById(transferDto.getFromAccount())
                .orElseThrow(() -> new AccountException("Account not found"));
        if (!fromAccount.getStatus().equals(TransactionConstants.AccountStatus.ACTIVE)) {
            throw new AccountException("Account status must be ACTIVE");
        }

        Account toAccount = accountRepository.findById(transferDto.getToAccount())
                .orElseThrow(() -> new AccountException("Account not found"));

        if (toAccount.getStatus() != TransactionConstants.AccountStatus.ACTIVE) {
            throw new AccountException("Destination account must be ACTIVE");
        }

        if (fromAccount.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        Transaction transaction = new Transaction();
        transaction.setType(TransactionConstants.TransactionType.TRANSFER);
        transaction.setAmount(transferDto.getAmount());
        transaction.setAccount(fromAccount);
        transaction.setToAccountId(toAccount.getId());
        transaction.setProcessedAt(LocalDateTime.now());

        fromAccount.setBalance(fromAccount.getBalance().subtract(transferDto.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transferDto.getAmount()));

        transaction.setStatus(TransactionConstants.TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        return modelMapper.map(fromAccount, AccountDto.class);

    }

}
