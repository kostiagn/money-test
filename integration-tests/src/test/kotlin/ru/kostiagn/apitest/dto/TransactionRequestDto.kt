package ru.kostiagn.apitest.dto

import java.math.BigDecimal

data class TransactionRequestDto(
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: BigDecimal
)


