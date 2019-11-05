package ru.kostiagn.apitest.rest

import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import ru.kostiagn.apitest.util.TestApp

object TransactionRestSpec {

    private val requestSpecification: RequestSpecification = RequestSpecBuilder()
        .setBaseUri(TestApp.url)
        .build()
        .log().all().filter(ResponseLoggingFilter())

    val spec
        get() =
            RestAssured.given()
                .spec(requestSpecification)
}
