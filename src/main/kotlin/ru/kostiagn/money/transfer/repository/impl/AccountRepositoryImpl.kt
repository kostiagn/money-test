package ru.kostiagn.money.transfer.repository.impl


import org.jooq.Configuration
import org.jooq.impl.DSL
import ru.kostiagn.money.transfer.persistence.Tables.ACCOUNT
import ru.kostiagn.money.transfer.persistence.enums.AccountStatus
import ru.kostiagn.money.transfer.persistence.tables.pojos.AccountPojo
import ru.kostiagn.money.transfer.repository.AccountRepository
import java.math.BigDecimal

class AccountRepositoryImpl : AccountRepository {
    override fun getAccount(cfg: Configuration, accountId: Long): AccountPojo =
        DSL.using(cfg).selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(accountId))
            .fetchOneIntoObj() ?: throw RecordNotFoundException("account with id $accountId not found")

    override fun getAccountWithLock(cfg: Configuration, accountId: Long): AccountPojo =
        DSL.using(cfg).selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(accountId))
            .forNoKeyUpdate()
            .fetchOneIntoObj() ?: throw RecordNotFoundException("account with id $accountId not found")

    override fun updateBalance(cfg: Configuration, accountId: Long, newBalance: BigDecimal) {
        DSL.using(cfg).update(ACCOUNT)
            .set(ACCOUNT.BALANCE, newBalance)
            .set(ACCOUNT.UPDATED, now())
            .where(ACCOUNT.ID.eq(accountId))
            .execute()
    }

    override fun updateStatus(cfg: Configuration, accountId: Long, newStatus: AccountStatus) {
        DSL.using(cfg).update(ACCOUNT)
            .set(ACCOUNT.STATUS, newStatus)
            .set(ACCOUNT.UPDATED, now())
            .where(ACCOUNT.ID.eq(accountId))
            .execute()
    }

    override fun create(cfg: Configuration, pojo: AccountPojo): AccountPojo {
        val now = now()
        return DSL.using(cfg).newRecord(ACCOUNT)
            .run {
                balance = pojo.balance ?: BigDecimal.ZERO
                status = pojo.status ?: AccountStatus.OPEN
                created = pojo.created ?: now
                updated = pojo.updated ?: now

                store()
                intoObj()
            }
    }
}