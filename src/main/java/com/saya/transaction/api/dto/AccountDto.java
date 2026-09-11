package com.saya.transaction.api.dto;

import com.saya.transaction.api.utils.TransactionConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {
    private Long id;
    private Long customerId;
    private BigDecimal balance;
    private String accountNickname;
    private String accountNumber;
    private TransactionConstants.AccountType accountType;
    private TransactionConstants.AccountStatus accountStatus;
    private LocalDateTime createdAt;

}
