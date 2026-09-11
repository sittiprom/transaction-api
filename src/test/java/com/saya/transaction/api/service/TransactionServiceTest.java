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
import com.saya.transaction.api.repository.TransactionRepository;

import com.saya.transaction.api.utils.TransactionConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TransactionService transactionService;



    @Test
    void withdrawal_shouldSucceed_whenBalanceIsSufficient() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal(20000));

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(100));
        transactionDto.setType(TransactionConstants.TransactionType.WITHDRAWAL);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));


        AccountDto accountDto = new AccountDto();
        accountDto.setId(1L);
        accountDto.setBalance(new BigDecimal(19900)); // 20000 - 100, decided by us, not read live
        when(modelMapper.map(account, AccountDto.class)).thenReturn(accountDto);

        // Act
        transactionService.withdrawalTransaction(transactionDto);

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction capturedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionConstants.TransactionStatus.COMPLETED,capturedTransaction.getStatus());
        assertEquals(TransactionConstants.TransactionType.WITHDRAWAL,capturedTransaction.getType());
        assertEquals(new BigDecimal(100),capturedTransaction.getAmount());
        assertEquals(new BigDecimal(19900), account.getBalance());

    }

    @Test
    void deposit_shouldSucceed() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal(500));

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(200));
        transactionDto.setType(TransactionConstants.TransactionType.DEPOSIT);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));


        AccountDto accountDto = new AccountDto();
        accountDto.setId(1L);
        accountDto.setBalance(new BigDecimal(700));
        when(modelMapper.map(account, AccountDto.class)).thenReturn(accountDto);


        transactionService.depositTransaction(transactionDto);

        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction capturedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionConstants.TransactionStatus.COMPLETED,capturedTransaction.getStatus());
        assertEquals(TransactionConstants.TransactionType.DEPOSIT,capturedTransaction.getType());
        assertEquals(new BigDecimal(200),capturedTransaction.getAmount());
        assertEquals(new BigDecimal(700), account.getBalance());


    }

    @Test
    void withdrawal_shouldThrow_whenBalanceIsInsufficient() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal(500));

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(2000));
        transactionDto.setType(TransactionConstants.TransactionType.WITHDRAWAL);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.withdrawalTransaction(transactionDto));
        verify(transactionRepository, never()).save(any(Transaction.class));

    }

    @Test
    void withdrawal_shouldThrow_InvalidTransactionException() {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(2000));
        transactionDto.setType(TransactionConstants.TransactionType.DEPOSIT);


        assertThrows(InvalidTransactionException.class, () -> transactionService.withdrawalTransaction(transactionDto));
        verify(transactionRepository, never()).save(any(Transaction.class));

    }

    @Test
    void withdrawal_shouldSucceed_whenBalanceIsEqualTransactionAmount() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("2000.00"));

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(2000));
        transactionDto.setType(TransactionConstants.TransactionType.WITHDRAWAL);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        transactionService.withdrawalTransaction(transactionDto);

        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(0,account.getBalance().compareTo(BigDecimal.ZERO));

    }

    @Test
    void withdrawal_shouldThrow_whenInvalidTransactionException() {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(0));
        transactionDto.setType(TransactionConstants.TransactionType.WITHDRAWAL);

        assertThrows(InvalidTransactionException.class, () -> transactionService.withdrawalTransaction(transactionDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldThrow_whenInvalidTransactionException() {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(200));
        transactionDto.setType(TransactionConstants.TransactionType.WITHDRAWAL);

        assertThrows(InvalidTransactionException.class, () -> transactionService.depositTransaction(transactionDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldThrowAccountException_whenAccountNotFound() {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(200));
        transactionDto.setType(TransactionConstants.TransactionType.DEPOSIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountException.class, () -> transactionService.depositTransaction(transactionDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldThrowAccountException_whenAccountIsNotActive() {

        Account account = new Account();
        account.setId(1L);
        account.setStatus(TransactionConstants.AccountStatus.CLOSED);
        account.setBalance(new BigDecimal(100));

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(1L);
        transactionDto.setAmount(new BigDecimal(200));
        transactionDto.setType(TransactionConstants.TransactionType.DEPOSIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThrows(AccountException.class, () -> transactionService.depositTransaction(transactionDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldSucceed() {
        Account toAccount = new Account();
        toAccount.setId(1L);
        toAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        toAccount.setBalance(new BigDecimal(1000));

        Account fromAccount = new Account();
        fromAccount.setId(2L);
        fromAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        fromAccount.setBalance(new BigDecimal(4000));

        TransferDto transferDto = new TransferDto();
        transferDto.setAmount(new BigDecimal(2000));
        transferDto.setFromAccount(2L);
        transferDto.setToAccount(1L);


        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(toAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(fromAccount));

        //Act
        transactionService.transferTransaction(transferDto);

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction capturedTransaction = transactionCaptor.getValue();


        assertEquals(TransactionConstants.TransactionStatus.COMPLETED,capturedTransaction.getStatus());
        assertEquals(TransactionConstants.TransactionType.TRANSFER,capturedTransaction.getType());
        assertEquals(new BigDecimal(2000),capturedTransaction.getAmount());

        assertEquals(new BigDecimal(3000), toAccount.getBalance());
        assertEquals(new BigDecimal(2000), fromAccount.getBalance());

    }

    @Test
    void transfer_shouldThrowInvalidTransactionException_whenAccountIsSame() {
        Account toAccount = new Account();
        toAccount.setId(1L);
        toAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        toAccount.setBalance(new BigDecimal(1000));

        TransferDto transferDto = new TransferDto();
        transferDto.setAmount(new BigDecimal(200));
        transferDto.setFromAccount(1L);
        transferDto.setToAccount(1L);

        //Act
        assertThrows(InvalidTransactionException.class, () -> transactionService.transferTransaction(transferDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldThrowInvalidTransactionException_whenAmountIsZero() {
        Account toAccount = new Account();
        toAccount.setId(1L);
        toAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        toAccount.setBalance(new BigDecimal(1000));

        TransferDto transferDto = new TransferDto();
        transferDto.setAmount(new BigDecimal(0));
        transferDto.setFromAccount(1L);
        transferDto.setToAccount(2L);

        //Act
        assertThrows(InvalidTransactionException.class, () -> transactionService.transferTransaction(transferDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldInsufficientBalanceException_whenBalanceIsLessThanAmount() {
        Account toAccount = new Account();
        toAccount.setId(1L);
        toAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        toAccount.setBalance(new BigDecimal(1000));

        Account fromAccount = new Account();
        fromAccount.setId(2L);
        fromAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        fromAccount.setBalance(new BigDecimal(500));

        TransferDto transferDto = new TransferDto();
        transferDto.setAmount(new BigDecimal(2000));
        transferDto.setFromAccount(2L);
        transferDto.setToAccount(1L);


        when(accountRepository.findById(1L)).thenReturn(Optional.of(toAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(fromAccount));

        //Act
        assertThrows(InsufficientBalanceException.class, () -> transactionService.transferTransaction(transferDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldThrowAccountException_whenFromAccountNotFound() {
        TransferDto transferDto = new TransferDto();
        transferDto.setAmount(new BigDecimal(2000));
        transferDto.setFromAccount(2L);
        transferDto.setToAccount(1L);

        when(accountRepository.findById(2L)).thenReturn(Optional.empty());

        //Act
        assertThrows(AccountException.class, () -> transactionService.transferTransaction(transferDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldThrowAccountException_whenToAccountNotFound() {

        Account fromAccount = new Account();
        fromAccount.setId(2L);
        fromAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        fromAccount.setBalance(new BigDecimal(500));

        TransferDto transferDto = new TransferDto();
        transferDto.setAmount(new BigDecimal(2000));
        transferDto.setFromAccount(2L);
        transferDto.setToAccount(1L);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        assertThrows(AccountException.class, () -> transactionService.transferTransaction(transferDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldThrowAccountException_whenToAccountIsNotActive() {

        Account toAccount = new Account();
        toAccount.setId(1L);
        toAccount.setStatus(TransactionConstants.AccountStatus.FROZEN);
        toAccount.setBalance(new BigDecimal(1000));

        Account fromAccount = new Account();
        fromAccount.setId(2L);
        fromAccount.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        fromAccount.setBalance(new BigDecimal(500));

        TransferDto transferDto = new TransferDto();
        transferDto.setAmount(new BigDecimal(2000));
        transferDto.setFromAccount(2L);
        transferDto.setToAccount(1L);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(toAccount));

        //Act
        assertThrows(AccountException.class, () -> transactionService.transferTransaction(transferDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldThrowAccountException_whenFromAccountIsNotActive() {

        Account fromAccount = new Account();
        fromAccount.setId(2L);
        fromAccount.setStatus(TransactionConstants.AccountStatus.CLOSED);
        fromAccount.setBalance(new BigDecimal(500));

        TransferDto transferDto = new TransferDto();
        transferDto.setAmount(new BigDecimal(2000));
        transferDto.setFromAccount(2L);
        transferDto.setToAccount(1L);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(fromAccount));

        //Act
        assertThrows(AccountException.class, () -> transactionService.transferTransaction(transferDto));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

}