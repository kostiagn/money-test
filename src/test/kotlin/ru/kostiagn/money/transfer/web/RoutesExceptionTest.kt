package ru.kostiagn.money.transfer.web

import com.google.inject.Guice
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import mu.KLogging
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.kostiagn.money.transfer.MainModule
import ru.kostiagn.money.transfer.module.ApplicationConfigModule
import ru.kostiagn.money.transfer.repository.impl.RecordNotFoundException
import ru.kostiagn.money.transfer.service.impl.AccountMustBeInOpenStatus
import ru.kostiagn.money.transfer.service.impl.NotEnoughMoney
import ru.kostiagn.money.transfer.service.impl.TransactionAmountMustBeGreaterThenZero
import ru.kostiagn.money.transfer.service.impl.TransactionMustBeInInitStatus
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail


@RunWith(Parameterized::class)
class RoutesExceptionTest(
    private val exception: Class<in RuntimeException>,
    private val httpStatusCode: HttpStatusCode,
    private val message: String?
) {
    companion object : KLogging() {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(RecordNotFoundException::class.java, HttpStatusCode.NotFound, null),
            arrayOf(TransactionMustBeInInitStatus::class.java, HttpStatusCode.ExpectationFailed, "transaction has closed yet"),
            arrayOf(TransactionAmountMustBeGreaterThenZero::class.java, HttpStatusCode.ExpectationFailed, "transaction amount must be greater then zero"),
            arrayOf(AccountMustBeInOpenStatus::class.java, HttpStatusCode.ExpectationFailed, "account is not in open status"),
            arrayOf(NotEnoughMoney::class.java, HttpStatusCode.ExpectationFailed, "there is not enough money for transaction"),
            arrayOf(RuntimeException::class.java, HttpStatusCode.InternalServerError, "the internal server error")
        )
    }

    private fun Application.routesExceptionTestModule() {
        Guice.createInjector(
            MainModule(this),
            ApplicationConfigModule()
        )
    }


    @Test
    fun `when RecordNotFoundException throws status code should be NotFound`() {
        withTestApplication({ routesExceptionTestModule() }) {
            application.routing {
                route("/test-exception") {
                    get("") {
                        throw initException(exception)
                    }
                }
            }
            handleRequest(HttpMethod.Get, "/test-exception").apply {
                if (message == null) {
                    assertNull(response.content)
                } else {
                    assertEquals(message, response.content)
                }
                assertEquals(httpStatusCode, response.status(), "exception $exception")

            }
        }
    }

    private fun initException(ex: Class<in RuntimeException>): RuntimeException {
        ex.constructors.firstOrNull { it.parameterCount == 0 }?.apply {
            return newInstance() as RuntimeException
        }
        ex.constructors.firstOrNull { it.parameterCount == 1 && it.parameters[0].type == String::class.java }?.apply {
            return newInstance("") as RuntimeException
        }
        ex.constructors.firstOrNull()?.apply {
            val values = parameters.map {
                when {
                    it.type == String::class.java -> ""
                    it.type == Long::class.java || it.type == java.lang.Long::class.java -> 0L
                    it.type == BigDecimal::class.java -> BigDecimal.ZERO
                    it.type.isEnum -> it.type.enumConstants[0]

                    else -> {
                        logger.error("unknow type ${it.type}")
                        fail("unknow type ${it.type}")
                    }
                }
            }.toTypedArray()
            return newInstance(*values) as RuntimeException
        }
        logger.error("not found constructor to create exception $ex")
        fail("not found constructor to create exception $ex")
    }
}


