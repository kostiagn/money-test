package ru.kostiagn.money.transfer.repository.impl

import kotlinx.coroutines.runBlocking
import org.jooq.impl.DSL
import org.junit.Test
import ru.kostiagn.money.transfer.AbstractTestWithPostgresContainer
import ru.kostiagn.money.transfer.persistence.Tables
import ru.kostiagn.money.transfer.persistence.enums.TransactionStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.AccountPojo
import ru.kostiagn.money.transfer.persistence.tables.pojos.TransactionPojo
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TransactionRepositoryImplTest : AbstractTestWithPostgresContainer() {

    @Test
    fun `test create and get transaction`() = runBlocking {
        val fromAcc = createAccount()
        val toAcc = createAccount()
        val pojo = TransactionPojo().apply {
            fromAccountId = fromAcc.id
            toAccountId = toAcc.id
            status = TransactionStatus.SUCCESS
            amount = BigDecimal("123.21")
            created = Timestamp.valueOf(LocalDateTime.now().minusMinutes(1))
            updated = Timestamp.valueOf(LocalDateTime.now().minusMinutes(2))
        }
        val created = inTx { cfg -> transactionRepository.create(cfg, pojo) }

        assertNotNull(created.id)
        pojo.id = created.id
        assertEquals(pojo, created)

        val selected = transactionRepository.getTransaction(jooqConfig, pojo.id)
        assertEquals(pojo, selected)

    }

    @Test
    fun `check created and updated after creating transaction`() = runBlocking {
        val before = LocalDateTime.now()
        val created = createTransaction()
        val after = LocalDateTime.now()

        val selected = transactionRepository.getTransaction(jooqConfig, created.id)


        assertEquals(selected.created, selected.updated)
        assertTrue(selected.created >= Timestamp.valueOf(before))
        assertTrue(selected.created <= Timestamp.valueOf(after))
    }

    @Test(expected = RecordNotFoundException::class)
    fun `getTransaction should throws exception when record not found`() {
        transactionRepository.getTransaction(jooqConfig, 111111)
    }


    @Test
    fun getTransactionWithLockTest() = runBlocking {

        val transactionIsFinished = AtomicBoolean(false)
        val recordIsLocked = AtomicBoolean(false)
        val checkIsFinished = AtomicBoolean(false)


        val transaction = createTransaction()

        Thread {
            runBlocking {
                inTx { cfg ->
                    transactionRepository.getTransactionWithLock(cfg, transaction.id)
                    recordIsLocked.set(true)
                    waitFor(checkIsFinished)
                }
                transactionIsFinished.set(true)
            }
        }.start()

        inTx { cfg ->
            waitFor(recordIsLocked)

            assertEquals(
                0, DSL.using(cfg).selectFrom(Tables.TRANSACTION)
                    .where(Tables.TRANSACTION.ID.eq(transaction.id))
                    .forNoKeyUpdate().skipLocked()
                    .count()
                , "record must be locked"
            )
            checkIsFinished.set(true)
            waitFor(transactionIsFinished)

            assertEquals(
                1, DSL.using(cfg).selectFrom(Tables.TRANSACTION)
                    .where(Tables.TRANSACTION.ID.eq(transaction.id))
                    .forNoKeyUpdate()
                    .count()
                , "record must be unlocked"
            )
        }
    }

    @Test(expected = RecordNotFoundException::class)
    fun `getTransactionWithLock should throws exception when record not found`() {
        transactionRepository.getTransactionWithLock(jooqConfig, 111111)
    }

    @Test
    fun updateStatusTest() = runBlocking {
        val transaction = createTransaction()

        val before = LocalDateTime.now()
        inTx { cfg ->
            transactionRepository.updateStatus(cfg, transaction.id, TransactionStatus.SUCCESS)
        }
        val after = LocalDateTime.now()


        val selected = transactionRepository.getTransaction(jooqConfig, transaction.id)


        assertEquals(TransactionStatus.SUCCESS, selected.status)
        assertTrue(selected.created < selected.updated)
        assertTrue(selected.updated >= Timestamp.valueOf(before))
        assertTrue(selected.updated <= Timestamp.valueOf(after))
    }

    private fun waitFor(flag: AtomicBoolean) {
        while (!flag.get()) {
            Thread.sleep(100)
        }
    }

    private suspend fun createAccount(balance: Long = 0): AccountPojo {
        return inTx { cfg ->
            accountRepository.create(cfg, AccountPojo().also {
                it.balance = BigDecimal.valueOf(balance)
            })
        }
    }

    private suspend fun createTransaction(
        status: TransactionStatus = TransactionStatus.INIT,
        amount: BigDecimal = BigDecimal.ZERO
    ): TransactionPojo {
        val fromAcc = createAccount(10)
        val toAcc = createAccount(0)
        return inTx { cfg ->
            transactionRepository.create(cfg, TransactionPojo().also {
                it.fromAccountId = fromAcc.id
                it.toAccountId = toAcc.id
                it.status = status
                it.amount = amount
            })
        }
    }
}