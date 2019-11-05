package ru.kostiagn.money.transfer.module

import com.google.inject.AbstractModule
import org.jooq.Configuration
import ru.kostiagn.money.transfer.config.DataSourceProvider
import ru.kostiagn.money.transfer.config.FlywayMigrate
import ru.kostiagn.money.transfer.config.JooqConfigProvider
import ru.kostiagn.money.transfer.repository.TransactionAware
import ru.kostiagn.money.transfer.repository.impl.TransactionAwareImpl
import javax.sql.DataSource


class DbModule : AbstractModule() {
    override fun configure() {
        bind(DataSource::class.java).toProvider(DataSourceProvider::class.java)
        bind(Configuration::class.java).toProvider(JooqConfigProvider::class.java)
        bind(TransactionAware::class.java).to(TransactionAwareImpl::class.java)
        bind(FlywayMigrate::class.java).asEagerSingleton()
    }
}