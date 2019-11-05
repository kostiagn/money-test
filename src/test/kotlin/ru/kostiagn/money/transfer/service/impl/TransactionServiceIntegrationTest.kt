package ru.kostiagn.money.transfer.service.impl


import kotlinx.coroutines.runBlocking
import org.jooq.impl.DSL
import org.junit.Test
import ru.kostiagn.money.transfer.AbstractTestWithPostgresContainer
import ru.kostiagn.money.transfer.persistence.Tables.TRANSACTION
import ru.kostiagn.money.transfer.persistence.enums.TransactionStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.AccountPojo
import ru.kostiagn.money.transfer.persistence.tables.pojos.TransactionPojo
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class TransactionServiceIntegrationTest : AbstractTestWithPostgresContainer() {
    @Test
    fun testTransferFromManyThread() {
        runBlocking {
            val one = inTx {
                accountRepository.create(
                    it,
                    AccountPojo().setBalance(BigDecimal.valueOf(1000000))
                )
            }
            val two = inTx {
                accountRepository.create(
                    it,
                    AccountPojo().setBalance(BigDecimal.valueOf(1000000))
                )
            }
            val three = inTx {
                accountRepository.create(
                    it,
                    AccountPojo().setBalance(BigDecimal.valueOf(1000000))
                )
            }
            val cnt = 50
            val t1 = Thread {
                repeat(cnt) { transfer(one.id, two.id, 53) }
            }
            t1.start()
            val t2 = Thread {
                repeat(cnt) { transfer(two.id, three.id, 37) }
            }
            t2.start()
            val t3 = Thread {
                repeat(cnt) { transfer(three.id, one.id, 101) }
            }
            t3.start()

            t1.join()
            t2.join()
            t3.join()


            val selected1 = inTx { accountRepository.getAccount(it, one.id) }
            val selected2 = inTx { accountRepository.getAccount(it, two.id) }
            val selected3 = inTx { accountRepository.getAccount(it, three.id) }

            assertNotNull(selected1)
            assertEquals(1000000 + (101 - 53) * cnt, selected1.balance.toInt())
            assertNotNull(selected2)
            assertEquals(1000000 + (53 - 37) * cnt, selected2.balance.toInt())
            assertNotNull(selected3)
            assertEquals(1000000 + (37 - 101) * cnt, selected3.balance.toInt())

            checkTransactionsCount(cnt, one.id)
            checkTransactionsCount(cnt, two.id)
            checkTransactionsCount(cnt, three.id)
        }
    }

    private fun checkTransactionsCount(transactionsCount: Int, accountId: Long) {
        assertEquals(
            transactionsCount,
            DSL.using(jooqConfig)
                .selectFrom(TRANSACTION)
                .where(TRANSACTION.FROM_ACCOUNT_ID.eq(accountId))
                .count()
        )
        assertEquals(
            transactionsCount,
            DSL.using(jooqConfig)
                .selectFrom(TRANSACTION)
                .where(TRANSACTION.TO_ACCOUNT_ID.eq(accountId))
                .count()
        )

    }

    private fun transfer(fromId: Long, toId: Long, amount: Int) {
        runBlocking {
            val trn = inTx {
                transactionRepository.create(
                    it,
                    TransactionPojo()
                        .setAmount(BigDecimal.valueOf(amount.toLong()))
                        .setFromAccountId(fromId)
                        .setToAccountId(toId)
                        .setStatus(TransactionStatus.INIT)
                )
            }
            transactionService.transfer(trn.id)
        }
    }


}