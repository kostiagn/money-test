package ru.kostiagn.money.transfer

import com.google.inject.AbstractModule
import com.google.inject.Guice
import org.jooq.Configuration
import org.junit.AfterClass
import org.junit.BeforeClass
import ru.kostiagn.money.transfer.config.DbProperties
import ru.kostiagn.money.transfer.container.PostgreSQLContainer
import ru.kostiagn.money.transfer.module.DbModule
import ru.kostiagn.money.transfer.module.RepositoryModule
import ru.kostiagn.money.transfer.module.ServiceModule
import ru.kostiagn.money.transfer.repository.AccountRepository
import ru.kostiagn.money.transfer.repository.TransactionAware
import ru.kostiagn.money.transfer.repository.TransactionRepository
import ru.kostiagn.money.transfer.service.TransactionService

open class AbstractTestWithPostgresContainer {
    companion object {
        lateinit var container: PostgreSQLContainer
        lateinit var jooqConfig: Configuration
        lateinit var transactionAware: TransactionAware

        lateinit var transactionRepository: TransactionRepository
        lateinit var accountRepository: AccountRepository
        lateinit var transactionService: TransactionService

        @BeforeClass
        @JvmStatic
        fun setup() {
            container = PostgreSQLContainer()
            val dbProperties = container.start()

            val injector = Guice.createInjector(
                object : AbstractModule() {
                    override fun configure() {
                        bind(DbProperties::class.java).toInstance(dbProperties)
                    }
                },
                DbModule(),
                RepositoryModule(),
                ServiceModule()
            )

            jooqConfig = injector.getInstance(Configuration::class.java)
            transactionRepository = injector.getInstance(TransactionRepository::class.java)
            accountRepository = injector.getInstance(AccountRepository::class.java)
            transactionService = injector.getInstance(TransactionService::class.java)
            transactionAware = injector.getInstance(TransactionAware::class.java)
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            container.stop()
        }
    }

    protected suspend fun <T> inTx(block: (configuration: Configuration) -> T): T = transactionAware.inTx(block)
}