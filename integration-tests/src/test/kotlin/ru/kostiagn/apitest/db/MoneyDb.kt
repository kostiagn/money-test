package ru.kostiagn.apitest.db


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KLogging
import org.nield.rxkotlinjdbc.execute
import org.nield.rxkotlinjdbc.select
import org.picocontainer.Startable
import ru.kostiagn.apitest.util.TestApp


class MoneyDb : Startable {
    companion object : KLogging()

    val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
        .registerModule(JavaTimeModule())


    lateinit var ds: HikariDataSource
    override fun start() {
        ds = HikariDataSource(HikariConfig().apply {
            jdbcUrl = TestApp.moneyDbJdbcUrl
            username = TestApp.moneyDbUsername
            password = TestApp.moneyDbPassword
            isAutoCommit = true
        })
    }

    override fun stop() {
        ds.close()
    }


    inline fun <reified T> selectOne(sql: String): T = selectList<T>(sql).first()

    inline fun <reified T> selectList(sql: String): MutableList<T> {
        logger.info("Execute sql: $sql")
        val isDataClass = T::class.isData
        val clazz = T::class
        val list = ds.select(sql).toSequence { rs ->
            if (isDataClass) {
                selectFromDataClass(rs, clazz)
            } else {
                """"${rs.getString(1)}""""
            }
        }.toList()
        val json = list.joinToString(prefix = "[", postfix = "]", separator = ",")
        return objectMapper.readValue<MutableList<T>>(json, objectMapper.typeFactory.constructCollectionType(List::class.java, clazz.java))

    }

    fun update(sql: String) {
        logger.info("Execute sql: $sql")
        ds.execute(sql).blockingGet()
    }

    inline fun <reified T> insert(sql: String) = selectOne<T>(sql)


}