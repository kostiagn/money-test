package ru.kostiagn.money.transfer.config

import com.google.inject.Inject
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class FlywayMigrate @Inject constructor(
    dataSource: DataSource
) {
    init {
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .apply {
                clean()
                migrate()
            }
    }
}