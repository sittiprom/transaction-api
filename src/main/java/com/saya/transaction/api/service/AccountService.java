package com.saya.transaction.api.service;

import com.saya.transaction.api.dto.AccountDto;
import com.saya.transaction.api.dto.CreateAccountRequest;
import com.saya.transaction.api.entity.Account;
import com.saya.transaction.api.entity.Customer;
import com.saya.transaction.api.exception.AccountException;
import com.saya.transaction.api.exception.InvalidAccountException;
import com.saya.transaction.api.utils.TransactionConstants;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.saya.transaction.api.repository.AccountRepository;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final CustomerService customerService;

    public AccountService(AccountRepository accountRepository, ModelMapper modelMapper, CustomerService customerService) {
        this.accountRepository = accountRepository;
        this.modelMapper = modelMapper;
        this.customerService = customerService;
    }

    public AccountDto createAccount(CreateAccountRequest request) {
        Customer customer = customerService.findCustomerById(request.customerId());

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNickname(request.accountNickname());

        if (request.accountType() == null) {
            throw new InvalidAccountException("Account type must not be null");
        }

        account.setAccountType(request.accountType());

        account.setAccountNumber(request.accountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(TransactionConstants.AccountStatus.ACTIVE);
        accountRepository.save(account);
        return modelMapper.map(account, AccountDto.class);

    }

    public AccountDto getAccountDetail(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountException("Account not found"));

        return modelMapper.map(account, AccountDto.class);

    }


}
