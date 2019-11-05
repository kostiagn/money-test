package ru.kostiagn.money.transfer.config

import com.google.inject.Inject
import com.google.inject.Provider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

class DataSourceProvider : Provider<DataSource> {
    @Inject
    lateinit var dbProperties: DbProperties

    override fun get(): DataSource =
        HikariDataSource(
            HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = dbProperties.url
                username = dbProperties.username
                password = dbProperties.password
                maximumPoolSize = 20
                isAutoCommit = false
                validate()
            })
}

