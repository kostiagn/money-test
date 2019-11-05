package ru.raiffeisen.rmcp.container

import org.testcontainers.containers.PostgreSQLContainer

data class DbProperties(val url: String, val username: String, val password: String)

fun createAndStartPostgres(): DbProperties {
    PostgreSQLContainer<Nothing>("postgres:10.5-alpine").apply {
        start()
        return DbProperties(
            "jdbc:postgresql://$containerIpAddress:$firstMappedPort/$databaseName",
            username,
            password
        )
    }
}


