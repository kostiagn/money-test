package ru.kostiagn.money.transfer.service.impl

import com.google.inject.Inject
import org.jooq.Configuration
import ru.kostiagn.money.transfer.dto.TransactionDto
import ru.kostiagn.money.transfer.dto.TransactionRequestDto
import ru.kostiagn.money.transfer.dto.toDto
import ru.kostiagn.money.transfer.persistence.enums.AccountStatus
import ru.kostiagn.money.transfer.persistence.enums.TransactionStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.AccountPojo
import ru.kostiagn.money.transfer.persistence.tables.pojos.TransactionPojo
import ru.kostiagn.money.transfer.repository.AccountRepository
import ru.kostiagn.money.transfer.repository.TransactionAware
import ru.kostiagn.money.transfer.repository.TransactionRepository
import ru.kostiagn.money.transfer.service.TransactionService
import java.math.BigDecimal

class TransactionServiceImpl @Inject constructor(
    private val transactionAware: TransactionAware,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : TransactionService {

    override suspend fun getTransaction(transactionId: Long): TransactionDto {
        return transactionAware.inTx { cfg: Configuration ->
            transactionRepository.getTransaction(cfg, transactionId)
                .toDto()
        }
    }

    override suspend fun initTransaction(transactionRequestDto: TransactionRequestDto): TransactionDto {
        if (transactionRequestDto.amount <= BigDecimal.ZERO) {
            throw TransactionAmountMustBeGreaterThenZero(null, transactionRequestDto.amount)
        }
        return transactionAware.inTx { cfg: Configuration ->
            accountRepository.getAccount(cfg, transactionRequestDto.fromAccountId).apply {
                if (status != AccountStatus.OPEN) {
                    throw AccountMustBeInOpenStatus(id, status)
                }
                if (balance < transactionRequestDto.amount) {
                    throw NotEnoughMoney(id, balance, transactionRequestDto.amount)
                }
            }

            accountRepository.getAccount(cfg, transactionRequestDto.toAccountId).apply {
                if (status != AccountStatus.OPEN) {
                    throw AccountMustBeInOpenStatus(id, status)
                }
            }

            transactionRepository.create(cfg, TransactionPojo().apply {
                fromAccountId = transactionRequestDto.fromAccountId
                toAccountId = transactionRequestDto.toAccountId
                amount = transactionRequestDto.amount
                status = TransactionStatus.INIT
            }).toDto()

        }
    }

    override suspend fun transfer(transactionId: Long) {
        try {
            transferInner(transactionId)
        } catch (e: FailTransactionException) {
            transactionAware.inTx { cfg: Configuration ->
                transactionRepository.updateStatus(cfg, transactionId, TransactionStatus.FAIL)
            }
            throw e
        }
    }

    private suspend fun transferInner(transactionId: Long) {
        transactionAware.inTx { cfg: Configuration ->
            val transaction = transactionRepository.getTransactionWithLock(cfg, transactionId)
            if (transaction.status != TransactionStatus.INIT) {
                throw TransactionMustBeInInitStatus(transaction.id, transaction.status)
            }

            val from: AccountPojo
            val to: AccountPojo
            if (transaction.fromAccountId > transaction.toAccountId) {
                from = lock(cfg, transaction.fromAccountId)
                to = lock(cfg, transaction.toAccountId)
            } else {
                to = lock(cfg, transaction.toAccountId)
                from = lock(cfg, transaction.fromAccountId)
            }
            if (from.balance < transaction.amount) {
                throw NotEnoughMoney(from.id, from.balance, transaction.amount)
            }
            accountRepository.updateBalance(cfg, from.id, from.balance.minus(transaction.amount))
            accountRepository.updateBalance(cfg, to.id, to.balance.plus(transaction.amount))
            transactionRepository.updateStatus(cfg, transactionId, TransactionStatus.SUCCESS)
        }
    }

    private fun lock(cfg: Configuration, accountId: Long): AccountPojo =
        accountRepository.getAccountWithLock(cfg, accountId).apply {
            if (status != AccountStatus.OPEN) {
                throw AccountMustBeInOpenStatus(id, status)
            }
        }
}




