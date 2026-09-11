package com.saya.transaction.api.dto;

import com.saya.transaction.api.utils.TransactionConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record CreateAccountRequest(

        @NotNull
        Long customerId,
        @NotBlank
        String accountNickname,
        @NotBlank
        String accountNumber,
        @NotNull
        TransactionConstants.AccountType accountType
) {

}
