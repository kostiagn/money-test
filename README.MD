# Money transfer service between accounts
Service has 3 endpoints:
* Start transaction: 
```
POST /transaction header:'Content-type: application-json'
body: {
  "fromAccountId": Long,
  "toAccountId": Long,
  "amount": BigDecimal
}

Response: 200 OK
{
 "id": Long, 
 "fromAccountId": Long,
 "toAccountId": Long,
 "amount": BigDecimal,
 "status": "INIT",
 "created": Timestamp
 "updated": Timestamp
}

Response: 404 if fromAccount or toAccount do not exist

Response: 417 with message
* 'account is not in open status' when fromAccount or toAccount do not have status 'OPEN'  
* 'there is not enough money for transaction' when fromAccount balace is less then transaction amount
* 'transaction amount must be greater then zero' when transaction amount is less or equals zero 
```

* Get transaction:
```
GET /transaction/{transaction-id}

Response: 200 OK
{
 "id": Long, 
 "fromAccountId": Long,
 "toAccountId": Long,
 "amount": BigDecimal,
 "status": "INIT" | "FAIL" | "SUCCESS",
 "created": Timestamp
 "updated": Timestamp
}

Response: 404 when transaction is not found in db
```


* Start transaction:
```
PUT /transaction/{transaction-id}

Response: 204 OK when transaction is successfully executed 

Response: 404 when transaction is not found in db

Response: 417 with message
* 'account is not in open status' when fromAccount or toAccount do not have status 'OPEN'  
* 'there is not enough money for transaction' when fromAccount balace is less then transaction amount
```



Prerequisites
-------------

```
java 8 or height, docker
```

Build
-----
```
./gradlew generateJooq clean build -x test
```

Project uses [JOOQ](http://www.jooq.org/doc/3.12/manual/), [Flyway](https://flywaydb.org/documentation/), [testcontainers with postgres module](https://www.testcontainers.org/modules/databases/postgres/)

Before compilation it needs to start jooq generation. For this
* Postgresql starts in docker container by testcontainers (task startCodeGenDb)
* Flyway executes migration scripts from folder src/main/resources/db/migration (task flywayClean flywayMigrate)
* Jooq generator starts (tast generateJooq)

Running the tests
-----------------
to start unit and integration tests
```
./gradlew test
```

Debug integration tests
-----------------------

Integration tests execute by class integration-tests/src/test/kotlin/ru/kostiagn/apitest/RunCucumberIT.kt

you can start service in debug mode and then run integeration tests RunCucumberIT

if service was not started RunCucumberIT try to find jar file in folder build/libs and start it. All output of started service will log will prefix >>>. Before start service RunCucumberIT starts postgres in docker container and passes database connection properties to the service 


