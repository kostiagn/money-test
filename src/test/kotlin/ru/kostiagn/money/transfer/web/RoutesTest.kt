package ru.kostiagn.money.transfer.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import ru.kostiagn.money.transfer.dto.TransactionDto
import ru.kostiagn.money.transfer.service.TransactionService
import ru.kostiagn.money.transfer.testModule
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoutesTest {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    @Test
    fun `get transaction success test`() {
        val transactionService = mock<TransactionService>()
        withTestApplication({ testModule(transactionService) }) {
            runBlocking {
                val json =
                    //language=json
                    """
                    {
                        "id" : 1,
                        "fromAccountId" : 2,
                        "toAccountId" : 3,
                        "status" : "INIT",
                        "amount" : 10,
                        "created" : 1572591032946,
                        "updated" : 1572591092947
                    }
                """.trimIndent()

                given(transactionService.getTransaction(1)).willReturn(
                    objectMapper.readValue<TransactionDto>(json)
                )


                handleRequest(HttpMethod.Get, "/transaction/1").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    JSONAssert.assertEquals(json, response.content, JSONCompareMode.STRICT)
                }
            }
        }
    }

    @Test
    fun `init transaction success test`() {
        val transactionService = mock<TransactionService>()
        withTestApplication({ testModule(transactionService) }) {
            runBlocking {
                val fromAccId = 10L
                val toAccId = 11L
                val amount = BigDecimal("10.01")
                val jsonResponse =
                    //language=json
                    """
                    {
                        "id" : 2,
                        "fromAccountId" : $fromAccId,
                        "toAccountId" : $toAccId,
                        "status" : "INIT",
                        "amount" : $amount,
                        "created" : 1572591032945,
                        "updated" : 1572591092946
                    }
                """.trimIndent()
                val jsonRequest =
                    //language=json
                    """
                    {
                        "fromAccountId" : $fromAccId,
                        "toAccountId" : $toAccId,
                        "amount" : $amount
                    }
                """.trimIndent()

                given(transactionService.initTransaction(objectMapper.readValue(jsonRequest))).willReturn(
                    objectMapper.readValue<TransactionDto>(jsonResponse)
                )

                handleRequest(HttpMethod.Post, "/transaction") {
                    addHeader("Content-type", "application/json")
                    setBody(jsonRequest)
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    JSONAssert.assertEquals(jsonResponse, response.content, JSONCompareMode.STRICT)
                }
            }
        }
    }

    @Test
    fun `transfer success test`() {
        val transactionService = mock<TransactionService>()
        withTestApplication({ testModule(transactionService) }) {
            runBlocking {
                val trnId = 22L

                handleRequest(HttpMethod.Put, "/transaction/$trnId").apply {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                    assertNull(response.content)

                }
                verify(transactionService, times(1)).transfer(trnId)
            }
        }
    }
}


