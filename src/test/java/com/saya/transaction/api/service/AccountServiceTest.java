package com.saya.transaction.api.service;

import com.saya.transaction.api.dto.AccountDto;
import com.saya.transaction.api.dto.CreateAccountRequest;
import com.saya.transaction.api.entity.Account;
import com.saya.transaction.api.entity.Customer;
import com.saya.transaction.api.entity.Transaction;
import com.saya.transaction.api.exception.AccountException;
import com.saya.transaction.api.exception.CustomerNotFoundException;
import com.saya.transaction.api.exception.InvalidAccountException;
import com.saya.transaction.api.repository.AccountRepository;
import com.saya.transaction.api.utils.TransactionConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.sql.Types.NULL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ModelMapper modelMapper;

    @Test
    void createAccountWithSuccess() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("test_customer@email");
        customer.setName("Test Customer");

        CreateAccountRequest request = new CreateAccountRequest(
                customer.getId(),
                "accountSaving",
                "AA00001",
                TransactionConstants.AccountType.SAVINGS
        );

        when(modelMapper.map(any(), any(Class.class))).thenReturn(new AccountDto());

        when(customerService.findCustomerById(request.customerId())).thenReturn(customer);


        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        accountService.createAccount(request);
        verify(accountRepository).save(accountCaptor.capture());

        Account capturedAccount = accountCaptor.getValue();
        assertEquals(request.accountNumber(), capturedAccount.getAccountNumber());
        assertEquals(request.accountNickname(), capturedAccount.getAccountNickname());
        assertEquals(request.accountType(), capturedAccount.getAccountType());
        assertEquals(TransactionConstants.AccountStatus.ACTIVE, capturedAccount.getStatus());
        assertEquals(TransactionConstants.AccountType.SAVINGS, capturedAccount.getAccountType());
        assertEquals(0, capturedAccount.getBalance().compareTo(BigDecimal.ZERO));

    }

    @Test
    void createAccountWithAccountTypeIsNull() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("test_customer@email");
        customer.setName("Test Customer");

        CreateAccountRequest request = new CreateAccountRequest(
                1L,
                "accountSaving",
                "AA00001",
                null
        );

        when(customerService.findCustomerById(request.customerId())).thenReturn(customer);

        assertThrows(InvalidAccountException.class, () -> accountService.createAccount(request));
        verify(accountRepository, never()).save(any(Account.class));

    }

    @Test
    void getAccountDetailWithSuccess() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("test_customer@email");
        customer.setName("Test Customer");

        Account account = new Account();
        account.setId(2L);
        account.setAccountNumber("accountNumber");
        account.setAccountNickname("accountNickname");
        account.setBalance(BigDecimal.TEN);
        account.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        account.setAccountType(TransactionConstants.AccountType.SAVINGS);
        account.setCustomer(customer);

        AccountDto accountDto = new AccountDto();
        accountDto.setId(account.getId());
        accountDto.setAccountNumber(account.getAccountNumber());
        accountDto.setAccountNickname(account.getAccountNickname());
        accountDto.setBalance(account.getBalance());
        accountDto.setAccountType(account.getAccountType());
        accountDto.setCustomerId(customer.getId());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(modelMapper.map(account, AccountDto.class)).thenReturn(accountDto);

        accountService.getAccountDetail(2L);

        verify(modelMapper).map(accountCaptor.capture(), eq(AccountDto.class));

        Account capturedAccount = accountCaptor.getValue();

        assertEquals(account.getAccountNumber(), capturedAccount.getAccountNumber());
        assertEquals(account.getAccountNickname(), capturedAccount.getAccountNickname());
        assertEquals(account.getBalance(), capturedAccount.getBalance());
        assertEquals(account.getAccountType(), capturedAccount.getAccountType());
        assertEquals(account.getId(), capturedAccount.getId());

    }

    @Test
    void getAccountDetailWithNotFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AccountException.class, () -> accountService.getAccountDetail(2L));

    }

    @Test
    void createAccountWithCustomerIdNotFound() {
        CreateAccountRequest request = new CreateAccountRequest(
                1L,
                "NickName",
                "AA001",
                TransactionConstants.AccountType.SAVINGS
        );

        when(customerService.findCustomerById(anyLong())).thenThrow(new CustomerNotFoundException("Customer not found"));

        assertThrows(CustomerNotFoundException.class, () -> accountService.createAccount(request));

    }
}