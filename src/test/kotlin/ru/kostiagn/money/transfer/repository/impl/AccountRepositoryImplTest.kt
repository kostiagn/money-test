package ru.kostiagn.money.transfer.repository.impl

import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.jooq.impl.DSL
import org.junit.Test
import ru.kostiagn.money.transfer.AbstractTestWithPostgresContainer
import ru.kostiagn.money.transfer.persistence.Tables
import ru.kostiagn.money.transfer.persistence.enums.AccountStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.AccountPojo
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AccountRepositoryImplTest : AbstractTestWithPostgresContainer() {
    companion object : KLogging()

    @Test
    fun `test create and get account`() = runBlocking {
        val pojo = AccountPojo().apply {
            balance = BigDecimal.TEN
            status = AccountStatus.CLOSED
            created = Timestamp.valueOf(LocalDateTime.now().minusMinutes(1))
            updated = Timestamp.valueOf(LocalDateTime.now().minusMinutes(2))
        }
        val created = inTx { cfg -> accountRepository.create(cfg, pojo) }

        assertNotNull(created.id)
        pojo.id = created.id
        assertEquals(pojo, created)

        val selected = accountRepository.getAccount(jooqConfig, pojo.id)
        assertEquals(pojo, selected)

    }

    @Test
    fun `create with default values`() = runBlocking {
        val before = LocalDateTime.now()
        val created = inTx { cfg -> accountRepository.create(cfg, AccountPojo()) }
        val after = LocalDateTime.now()

        val selected = accountRepository.getAccount(jooqConfig, created.id)


        assertEquals(BigDecimal.ZERO, selected.balance)
        assertEquals(AccountStatus.OPEN, selected.status)
        assertEquals(selected.created, selected.updated)
        assertTrue(selected.created >= Timestamp.valueOf(before))
        assertTrue(selected.created <= Timestamp.valueOf(after))
    }

    @Test(expected = RecordNotFoundException::class)
    fun `getAccount should throws exception when record not found`() {
        accountRepository.getAccount(jooqConfig, 111111)
    }


    @Test
    fun getAccountWithLockTest() = runBlocking {

        val transactionIsFinished = AtomicBoolean(false)
        val recordIsLocked = AtomicBoolean(false)
        val checkIsFinished = AtomicBoolean(false)


        val account = inTx { cfg -> accountRepository.create(cfg, AccountPojo()) }

        Thread {
            runBlocking {
                inTx { cfg ->
                    accountRepository.getAccountWithLock(cfg, account.id)
                    recordIsLocked.set(true)
                    waitFor(checkIsFinished)
                }
                transactionIsFinished.set(true)
            }
        }.start()

        inTx { cfg ->
            waitFor(recordIsLocked)

            assertEquals(
                0, DSL.using(cfg).selectFrom(Tables.ACCOUNT)
                    .where(Tables.ACCOUNT.ID.eq(account.id))
                    .forNoKeyUpdate().skipLocked()
                    .count()
                , "record must be locked"
            )
            checkIsFinished.set(true)
            waitFor(transactionIsFinished)

            assertEquals(
                1, DSL.using(cfg).selectFrom(Tables.ACCOUNT)
                    .where(Tables.ACCOUNT.ID.eq(account.id))
                    .forNoKeyUpdate()
                    .count()
                , "record must be unlocked"
            )
        }
    }

    @Test(expected = RecordNotFoundException::class)
    fun `getAccountWithLock should throws exception when record not found`() {
        accountRepository.getAccountWithLock(jooqConfig, 111111)
    }

    @Test
    fun updateBalanceTest() = runBlocking {
        val account = inTx { cfg ->
            accountRepository.create(cfg, AccountPojo().apply { balance = BigDecimal("123.123") })
        }

        val before = LocalDateTime.now()
        inTx { cfg ->
            accountRepository.updateBalance(cfg, account.id, BigDecimal("777.777"))
        }
        val after = LocalDateTime.now()


        val selected = accountRepository.getAccount(jooqConfig, account.id)


        assertEquals(BigDecimal("777.777"), selected.balance)
        assertTrue(selected.created < selected.updated)
        assertTrue(selected.updated >= Timestamp.valueOf(before))
        assertTrue(selected.updated <= Timestamp.valueOf(after))


    }


    @Test
    fun updateStatusTest() = runBlocking {
        val account = inTx { cfg ->
            accountRepository.create(cfg, AccountPojo().apply { status = AccountStatus.OPEN })
        }

        val before = LocalDateTime.now()
        inTx { cfg ->
            accountRepository.updateStatus(cfg, account.id, AccountStatus.CLOSED)
        }
        val after = LocalDateTime.now()


        val selected = accountRepository.getAccount(jooqConfig, account.id)


        assertEquals(AccountStatus.CLOSED, selected.status)
        assertTrue(selected.created < selected.updated)
        assertTrue(selected.updated >= Timestamp.valueOf(before))
        assertTrue(selected.updated <= Timestamp.valueOf(after))
    }

    private fun waitFor(flag: AtomicBoolean) {
        while (!flag.get()) {
            Thread.sleep(100)
        }
    }


}