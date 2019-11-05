package ru.kostiagn.money.transfer.service

import ru.kostiagn.money.transfer.dto.TransactionDto
import ru.kostiagn.money.transfer.dto.TransactionRequestDto

interface TransactionService {
    suspend fun getTransaction(transactionId: Long): TransactionDto
    suspend fun initTransaction(transactionRequestDto: TransactionRequestDto): TransactionDto
    suspend fun transfer(transactionId: Long)
}