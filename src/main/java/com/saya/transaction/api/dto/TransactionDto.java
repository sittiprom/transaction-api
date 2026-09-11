package com.saya.transaction.api.dto;

import com.saya.transaction.api.entity.Account;
import com.saya.transaction.api.utils.TransactionConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private Long id;

    @NotNull
    private Long accountId;

    private TransactionConstants.TransactionType type;

    @NotNull
    @Positive
    private BigDecimal amount;

    private TransactionConstants.TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
