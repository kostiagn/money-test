package ru.kostiagn.money.transfer.config

import com.google.inject.Inject
import com.google.inject.Provider
import org.jooq.Configuration
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import javax.sql.DataSource

class JooqConfigProvider : Provider<Configuration> {
    @Inject
    lateinit var dataSource: DataSource

    override fun get(): Configuration =
        DefaultConfiguration().set(SQLDialect.POSTGRES).set(dataSource)
}

