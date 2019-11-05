package ru.kostiagn.money.transfer.container

import mu.KLogging
import org.testcontainers.containers.PostgreSQLContainer
import ru.kostiagn.money.transfer.config.DbProperties

class PostgreSQLContainer {
    companion object : KLogging()

    lateinit var container: PostgreSQLContainer<*>
    fun start(): DbProperties =
        PostgreSQLContainer<Nothing>("postgres:10.5-alpine").let { container ->
            this.container = container
            container.start()
            DbProperties(
                url = container.jdbcUrl,
                username = container.username,
                password = container.password
            ).also {
                logger.info("Postgres container has started. Connection properties are $it")
            }
        }

    fun stop() {
        container.stop()
    }
}