package ru.kostiagn.money.transfer.repository.impl

import com.google.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.jooq.Configuration
import org.jooq.impl.DSL
import ru.kostiagn.money.transfer.repository.TransactionAware


class TransactionAwareImpl : TransactionAware {
    @Inject
    private lateinit var jooqConfig: Configuration

    override suspend fun <T> inTx(block: (configuration: Configuration) -> T): T =
        withContext(Dispatchers.IO) {
            DSL.using(jooqConfig).transactionResultAsync { cfg: Configuration ->
                block(cfg)
            }.await()
        }
}
