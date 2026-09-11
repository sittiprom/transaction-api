package com.saya.transaction.api.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;


import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferDto {
    @NotNull
    private Long fromAccount;

    @NotNull
    private Long toAccount;

    @NotNull
    @Positive
    private BigDecimal amount;
}
