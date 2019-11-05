package ru.kostiagn.apitest.step


import io.cucumber.java8.En
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import ru.kostiagn.apitest.dao.AccountDao
import ru.kostiagn.apitest.dao.TransactionDao
import ru.kostiagn.apitest.dto.AccountDto
import ru.kostiagn.apitest.dto.AccountStatus
import ru.kostiagn.apitest.dto.TransactionDto
import ru.kostiagn.apitest.dto.TransactionRequestDto
import ru.kostiagn.apitest.dto.TransactionStatus
import ru.kostiagn.apitest.rest.EndPoints
import ru.kostiagn.apitest.rest.TransactionRestSpec
import ru.kostiagn.apitest.rest.restObjectMapper
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class TransactionStep(
    accountDao: AccountDao,
    transactionDao: TransactionDao
) : En {
    private lateinit var fromAccount: AccountDto
    private lateinit var toAccount: AccountDto
    private lateinit var response: ValidatableResponse
    private lateinit var transactionDto: TransactionDto

    private fun getAccount(accountNumber: String) = when (accountNumber) {
        "first" -> fromAccount
        "second" -> toAccount
        else -> throw RuntimeException("wrong accountNumber $accountNumber")

    }

    init {
        Given("^Exists (.+) account with balance (\\S+)$") { accountNumber: String, balance: BigDecimal ->
            val account = accountDao.insertAccount(balance)
            when (accountNumber) {
                "first" -> fromAccount = account
                "second" -> toAccount = account
                else -> throw RuntimeException("wrong accountNumber $accountNumber")
            }
        }

        Given("^Exists (.+) account with balance (.+) with status (.+)$") { accountNumber: String, balance: BigDecimal, status: AccountStatus ->
            val account = accountDao.insertAccount(balance, status)
            when (accountNumber) {
                "first" -> fromAccount = account
                "second" -> toAccount = account
                else -> throw RuntimeException("wrong accountNumber $accountNumber")
            }
        }

        When("^Client initializes transaction from (.+) account to (.+) account with amount (.+)$") { fromAccNum: String, toAccNum: String, amount: BigDecimal ->
            response = TransactionRestSpec.spec
                .contentType(ContentType.JSON)
                .body(
                    TransactionRequestDto(
                        fromAccountId = getAccount(fromAccNum).id,
                        toAccountId = getAccount(toAccNum).id,
                        amount = amount
                    ), restObjectMapper
                ).post(EndPoints.Transaction.POST)
                .then()
            if (response.extract().statusCode() == 200) {
                transactionDto = response.extract().body().`as`(TransactionDto::class.java)
            }

        }

        When("^Balance was changed to (.+) and status was changed to (.+) for (.+) account$") { balance: BigDecimal, status: AccountStatus, accountNumber: String ->
            val account = getAccount(accountNumber)
            accountDao.updateAccount(account.id, balance, status)
        }

        When("^Client commits transaction$") {
            response = TransactionRestSpec.spec
                .pathParam("transactionId", transactionDto.id)
                .put(EndPoints.Transaction.PUT)
                .then()
        }

        When("^Client gets transaction$") {
            response = TransactionRestSpec.spec
                .pathParam("transactionId", transactionDto.id)
                .get(EndPoints.Transaction.GET)
                .then()

        }

        Then("^System responds with (.*) code$") { statusCode: String ->
            when (statusCode) {
                "success" -> response.statusCode(200)
                "noContent" -> response.statusCode(204)
                "expectationFailed" -> response.statusCode(417)
                else -> throw RuntimeException("unknown status code $statusCode")
            }
        }

        Then("^Client sees error message (.*)$") { errorMessage: String ->
            val body = response.extract().body().asString()
            if (errorMessage.isBlank()) {
                assertNull(body)
            } else {
                assertEquals(errorMessage, body)
            }
        }


        Then("^Client sees the transaction with amount (.+) and in status (.+)$") { amount: BigDecimal, transactionStatus: TransactionStatus ->

            assertNotNull(transactionDto)
            assertNotNull(transactionDto.id)
            assertEquals(transactionStatus, transactionDto.status)
            assertEquals(amount, transactionDto.amount)
        }

        Then("^Balance of the (.+) account is (.+)") { accountNumber: String, balance: BigDecimal ->
            val account = when (accountNumber) {
                "first" -> accountDao.selectAccount(fromAccount.id)
                "second" -> accountDao.selectAccount(toAccount.id)
                else -> throw RuntimeException("wrong accountNumber $accountNumber")
            }

            assertEquals(balance, account.balance)
        }

        Then("^Transaction has status (.+)$") { status: TransactionStatus ->
            transactionDao.selectTransaction(transactionDto.id).also {
                assertEquals(status, it.status)
            }
        }

    }
}