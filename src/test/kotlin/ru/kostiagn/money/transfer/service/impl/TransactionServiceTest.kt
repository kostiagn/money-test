package ru.kostiagn.money.transfer.service.impl

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import ru.kostiagn.money.transfer.dto.TransactionRequestDto
import ru.kostiagn.money.transfer.persistence.enums.AccountStatus
import ru.kostiagn.money.transfer.persistence.enums.TransactionStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.AccountPojo
import ru.kostiagn.money.transfer.persistence.tables.pojos.TransactionPojo
import ru.kostiagn.money.transfer.repository.AccountRepository
import ru.kostiagn.money.transfer.repository.TransactionAware
import ru.kostiagn.money.transfer.repository.TransactionRepository
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class TransactionServiceTest {

    private val transactionAware: TransactionAware = TransactionAwareDummyImpl()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionService: TransactionServiceImpl

    @Before
    fun init() {
        transactionRepository = mock()
        accountRepository = mock()
        transactionService = TransactionServiceImpl(
            transactionAware,
            transactionRepository,
            accountRepository
        )
    }

    @Test(expected = TransactionMustBeInInitStatus::class)
    fun `transfer should throw exception when transaction status is SUCCESS`() = runBlocking {
        val trnId = 10L
        given(transactionRepository.getTransactionWithLock(any(), eq(trnId)))
            .willReturn(TransactionPojo().apply {
                id = trnId
                status = TransactionStatus.SUCCESS
            })

        transactionService.transfer(trnId)
    }


    @Test(expected = TransactionMustBeInInitStatus::class)
    fun `transfer should throw exception when transaction status is FAIL`() = runBlocking {
        val trnId = 10L
        given(transactionRepository.getTransactionWithLock(any(), eq(trnId)))
            .willReturn(TransactionPojo().apply {
                id = trnId
                status = TransactionStatus.FAIL
            })

        transactionService.transfer(trnId)
    }

    @Test(expected = AccountMustBeInOpenStatus::class)
    fun `transfer should throw exception when fromAccount is in status CLOSED`() = runBlocking {
        val trnId = 10L
        val fromId = 11L
        val toId = 22L
        prepare(
            TransactionPojo().setId(trnId),
            AccountPojo().setId(fromId).setStatus(AccountStatus.CLOSED),
            AccountPojo().setId(toId)
        )

        transactionService.transfer(trnId)
    }

    @Test
    fun `transfer should set transaction status to FAIL when fromAccount is in status CLOSED`() = runBlocking {
        val trnId = 10L
        val fromId = 11L
        val toId = 22L
        prepare(
            TransactionPojo().setId(trnId),
            AccountPojo().setId(fromId).setStatus(AccountStatus.CLOSED),
            AccountPojo().setId(toId)
        )

        try {
            transactionService.transfer(trnId)
        } catch (e: Exception) {

        }

        verify(transactionRepository, times(1)).updateStatus(any(), eq(trnId), eq(TransactionStatus.FAIL))
    }

    @Test(expected = AccountMustBeInOpenStatus::class)
    fun `transfer should throw exception when toAccount is in status CLOSED`() = runBlocking {
        val trnId = 10L
        val fromId = 11L
        val toId = 22L
        prepare(
            TransactionPojo().setId(trnId),
            AccountPojo().setId(fromId),
            AccountPojo().setId(toId).setStatus(AccountStatus.CLOSED)
        )

        transactionService.transfer(trnId)
    }

    @Test
    fun `transfer should set transaction status to FAIL when toAccount is in status CLOSED`() = runBlocking {
        val trnId = 10L
        val fromId = 11L
        val toId = 22L
        prepare(
            TransactionPojo().setId(trnId),
            AccountPojo().setId(fromId),
            AccountPojo().setId(toId).setStatus(AccountStatus.CLOSED)
        )

        try {
            transactionService.transfer(trnId)
        } catch (e: Exception) {

        }

        verify(transactionRepository, times(1)).updateStatus(any(), eq(trnId), eq(TransactionStatus.FAIL))
    }

    @Test(expected = NotEnoughMoney::class)
    fun `transfer should throw exception when there is not enough money in fromAccount`() = runBlocking {
        val trnId = 10L
        val fromId = 11L
        val toId = 22L
        prepare(
            TransactionPojo().setId(trnId).setAmount(BigDecimal.TEN),
            AccountPojo().setId(fromId).setBalance(BigDecimal.ONE),
            AccountPojo().setId(toId)
        )

        transactionService.transfer(trnId)
    }

    @Test
    fun `transfer should set transaction status to FAIL when there is not enough money in fromAccount`() = runBlocking {
        val trnId = 10L
        val fromId = 11L
        val toId = 22L
        prepare(
            TransactionPojo().setId(trnId).setAmount(BigDecimal.TEN),
            AccountPojo().setId(fromId).setBalance(BigDecimal.ONE),
            AccountPojo().setId(toId)
        )

        try {
            transactionService.transfer(trnId)
        } catch (e: Exception) {

        }

        verify(transactionRepository, times(1)).updateStatus(any(), eq(trnId), eq(TransactionStatus.FAIL))
    }


    @Test
    fun `transfer should lock fromTransaction first when fromTransactionId is greater then toTransactionId`() {
        runBlocking {
            val trnId = 10L
            val fromId = 22L
            val toId = 11L
            prepare(TransactionPojo().setId(trnId), AccountPojo().setId(fromId), AccountPojo().setId(toId))


            transactionService.transfer(trnId)


            val orderVerifier = Mockito.inOrder(accountRepository)
            orderVerifier.verify(accountRepository).getAccountWithLock(any(), eq(fromId))
            orderVerifier.verify(accountRepository).getAccountWithLock(any(), eq(toId))
        }
    }

    @Test
    fun `transfer should lock toTransaction first when toTransactionId is greater then fromTransactionId`() {
        runBlocking {
            val trnId = 10L
            val fromId = 11L
            val toId = 22L
            prepare(TransactionPojo().setId(trnId), AccountPojo().setId(fromId), AccountPojo().setId(toId))


            transactionService.transfer(trnId)


            val orderVerifier = Mockito.inOrder(accountRepository)
            orderVerifier.verify(accountRepository).getAccountWithLock(any(), eq(toId))
            orderVerifier.verify(accountRepository).getAccountWithLock(any(), eq(fromId))
        }
    }


    @Test
    fun `success transfer`() = runBlocking {
        val trn = TransactionPojo().setId(10).setAmount(BigDecimal.TEN)
        val fromAcc = AccountPojo().setId(11).setBalance(BigDecimal.TEN)
        val toAcc = AccountPojo().setId(22).setBalance(BigDecimal.ONE)
        prepare(trn, fromAcc, toAcc)


        transactionService.transfer(trn.id)


        verify(transactionRepository, times(1)).updateStatus(any(), eq(trn.id), eq(TransactionStatus.SUCCESS))
        verify(accountRepository, times(1)).updateBalance(any(), eq(fromAcc.id), eq(BigDecimal.valueOf(0)))
        verify(accountRepository, times(1)).updateBalance(any(), eq(toAcc.id), eq(BigDecimal.valueOf(11)))
    }


    @Test
    fun `get transaction`() = runBlocking {
        val trn = TransactionPojo().apply {
            id = 10
            fromAccountId = 11
            toAccountId = 12
            status = TransactionStatus.SUCCESS
            amount = BigDecimal.ZERO
            created = Timestamp.valueOf(LocalDateTime.now().minusHours(1))
            updated = Timestamp.valueOf(LocalDateTime.now())
        }
        given(transactionRepository.getTransaction(any(), eq(trn.id))).willReturn(trn)


        val dto = transactionService.getTransaction(trn.id)


        verify(transactionRepository, times(1)).getTransaction(any(), eq(trn.id))
        assertNotNull(dto)
        assertEquals(trn.fromAccountId, dto.fromAccountId)
        assertEquals(trn.toAccountId, dto.toAccountId)
        assertEquals(trn.status, dto.status)
        assertEquals(trn.amount, dto.amount)
        assertEquals(trn.created, dto.created)
        assertEquals(trn.updated, dto.updated)

    }


    @Test(expected = TransactionAmountMustBeGreaterThenZero::class)
    fun `initTransfer should throw exception when amount is less or equal zero`() {
        runBlocking {
            transactionService.initTransaction(TransactionRequestDto(11, 22, BigDecimal.ZERO))
        }
    }

    @Test(expected = AccountMustBeInOpenStatus::class)
    fun `initTransfer should throw exception when fromAccount is closed`() {
        runBlocking {
            val trnId = 10L
            val fromId = 11L
            val toId = 22L
            prepare(
                TransactionPojo().setId(trnId),
                AccountPojo().setId(fromId).setStatus(AccountStatus.CLOSED),
                AccountPojo().setId(toId)
            )

            transactionService.initTransaction(TransactionRequestDto(fromId, toId, BigDecimal.TEN))
        }
    }

    @Test(expected = NotEnoughMoney::class)
    fun `initTransfer should throw exception when there is not enough money`() {
        runBlocking {
            val trnId = 10L
            val fromId = 11L
            val toId = 22L
            prepare(
                TransactionPojo().setId(trnId),
                AccountPojo().setId(fromId).setBalance(BigDecimal.ZERO),
                AccountPojo().setId(toId)
            )

            transactionService.initTransaction(TransactionRequestDto(fromId, toId, BigDecimal.TEN))
        }
    }

    @Test(expected = AccountMustBeInOpenStatus::class)
    fun `initTransfer should throw exception when toAccount is closed`() {
        runBlocking {
            val trnId = 10L
            val fromId = 11L
            val toId = 22L
            prepare(
                TransactionPojo().setId(trnId),
                AccountPojo().setId(fromId),
                AccountPojo().setId(toId).setStatus(AccountStatus.CLOSED)
            )

            transactionService.initTransaction(TransactionRequestDto(fromId, toId, BigDecimal.TEN))
        }
    }

    @Test
    fun `initTransfer success`() {
        runBlocking {
            val trnId = 10L
            val fromId = 11L
            val toId = 22L
            val trnAmount = BigDecimal.ONE
            prepare(
                TransactionPojo().setId(trnId).setAmount(trnAmount),
                AccountPojo().setId(fromId).setBalance(BigDecimal.TEN),
                AccountPojo().setId(toId)
            )

            val transactionPojo = TransactionPojo().apply {
                id = trnId
                fromAccountId = fromId
                toAccountId = toId
                status = TransactionStatus.INIT
                amount = trnAmount
                created = Timestamp.valueOf(LocalDateTime.now())
                updated = Timestamp.valueOf(LocalDateTime.now().minusDays(1))
            }
            given(
                transactionRepository.create(
                    any(),
                    eq(TransactionPojo().setFromAccountId(fromId).setToAccountId(toId).setAmount(trnAmount).setStatus(TransactionStatus.INIT))
                )
            ).willReturn(transactionPojo)


            val transactionDto = transactionService.initTransaction(TransactionRequestDto(fromId, toId, trnAmount))


            assertEquals(trnId, transactionDto.id)
            assertEquals(fromId, transactionDto.fromAccountId)
            assertEquals(toId, transactionDto.toAccountId)
            assertEquals(TransactionStatus.INIT, transactionDto.status)
            assertEquals(trnAmount, transactionDto.amount)
            assertEquals(transactionPojo.created, transactionDto.created)
            assertEquals(transactionPojo.updated, transactionDto.updated)


        }
    }

    //        return transactionAware.inTx { cfg: Configuration ->
//            accountRepository.getAccount(cfg, transactionPojo.fromAccountId).apply {
//                if (status != AccountStatus.OPEN) {
//                    throw AccountMustBeInOpenStatus(id, status)
//                }
//                if (balance < transactionPojo.amount) {
//                    throw NotEnoughMoney(id, balance, transactionPojo.amount)
//                }
//            }
//
//            accountRepository.getAccount(cfg, transactionPojo.toAccountId).apply {
//                if (status != AccountStatus.OPEN) {
//                    throw AccountMustBeInOpenStatus(id, status)
//                }
//            }
//
//            transactionRepository.create(cfg, TransactionPojo().apply {
//                fromAccountId = transactionPojo.fromAccountId
//                toAccountId = transactionPojo.toAccountId
//                amount = transactionPojo.amount
//            }).toDto()
//
//        }
//    }
    private fun prepare(transaction: TransactionPojo, fromAccount: AccountPojo, toAccount: AccountPojo) {
        val trn = TransactionPojo().apply {
            id = transaction.id
            status = transaction.status ?: TransactionStatus.INIT
            fromAccountId = fromAccount.id
            toAccountId = toAccount.id
            amount = transaction.amount ?: BigDecimal.TEN
        }

        val fromAcc = AccountPojo().apply {
            id = fromAccount.id
            status = fromAccount.status ?: AccountStatus.OPEN
            balance = fromAccount.balance ?: BigDecimal.TEN
        }

        val toAcc = AccountPojo().apply {
            id = toAccount.id
            status = toAccount.status ?: AccountStatus.OPEN
            balance = toAccount.balance ?: BigDecimal.ZERO
        }

        given(transactionRepository.getTransactionWithLock(any(), eq(trn.id))).willReturn(trn)
        given(accountRepository.getAccountWithLock(any(), eq(toAcc.id))).willReturn(toAcc)
        given(accountRepository.getAccount(any(), eq(toAcc.id))).willReturn(toAcc)
        given(accountRepository.getAccountWithLock(any(), eq(fromAcc.id))).willReturn(fromAcc)
        given(accountRepository.getAccount(any(), eq(fromAcc.id))).willReturn(fromAcc)
    }
}

class TransactionAwareDummyImpl : TransactionAware {
    override suspend fun <T> inTx(block: (configuration: Configuration) -> T): T = block(mock())
}