package ru.kostiagn.apitest


import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import io.restassured.RestAssured
import mu.KLogging
import org.awaitility.kotlin.await
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import ru.kostiagn.apitest.util.ProcessWrapper
import ru.kostiagn.apitest.util.TestApp
import ru.raiffeisen.rmcp.container.createAndStartPostgres
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.test.fail


@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["src/test/resources/features"],
    glue = ["ru.kostiagn.apitest.step"]
)
class RunCucumberIT {
    companion object : KLogging() {
        private var processWrapper: ProcessWrapper? = null

        @AfterClass
        @JvmStatic
        fun afterClass() {
            processWrapper?.terminate()
        }

        @BeforeClass
        @JvmStatic
        fun before() {
            try {
                beforeInner()
            } catch (e: Exception) {
                logger.error("error while starting test", e)
                throw e
            }
        }

        private fun beforeInner() {
            TestApp.init()
            if (TestApp.LOCAL_ENV == TestApp.env) {
                if (checkRunApplicationFromIDEA()) {
                    logger.info("***********************************************************************")
                    logger.info("***********     Application is running by IDEA      *******************")
                    logger.info("***********************************************************************")
                } else {
                    zombieCleanup()
                    val dbProperties = createAndStartPostgres()
                    logger.info("Postgres container has started. Connection properties are $dbProperties")

                    System.setProperty("MONEY_DB_URL", dbProperties.url)
                    System.setProperty("MONEY_DB_USERNAME", dbProperties.username)
                    System.setProperty("MONEY_DB_PASSWORD", dbProperties.password)
                    val dbEnv = arrayOf(
                        "-P:ktor.database.url=${dbProperties.url}",
                        "-P:ktor.database.username=${dbProperties.username}",
                        "-P:ktor.database.password=${dbProperties.password}"
                    )
                    runApp(*dbEnv)
                }
                awaitStartup("${TestApp.url}/version", 60)
            }
        }

        private fun runApp(vararg env: String) {
            val cmd =
                "java -jar ${getExecutableJarFile()} -config ${findLocalConfFile()} ${env.joinToString(" ")}"

            logger.info("run $cmd")
            processWrapper = ProcessWrapper(cmd)
        }

        private fun getExecutableJarFile(): Path =
            Files.list(getRootDir().resolve("build/libs"))
                .filter { it.fileName.toString().matches(Regex("^${TestApp.APP_NAME}-.*\\.jar$")) }
                .findFirst()
                .orElseThrow { RuntimeException("Unable to find executable product file") }
                .toAbsolutePath()

        private fun findLocalConfFile() =
            Files.list(getRootDir().resolve("${TestApp.INTEGRATION_TEST_DIR}/src/test/resources"))
                .filter { it.fileName.toString().matches(Regex("^application-local\\.conf$")) }
                .findFirst()
                .orElseThrow { RuntimeException("Unable to find application-local.conf") }
                .toAbsolutePath()


        private fun getRootDir(): Path {
            val root = System.getProperty("user.dir").trimEnd('/')
            if (root.endsWith(TestApp.INTEGRATION_TEST_DIR)) {
                return Paths.get(root.substring(0, root.length - TestApp.INTEGRATION_TEST_DIR.length))
            }
            return Paths.get(root)
        }

        private fun zombieCleanup() {
            val zombieKill = ProcessWrapper("ps -ef | grep ${TestApp.APP_NAME}\\S*jar | grep -v grep | awk '{ print $2}' | xargs -r kill -9")
            zombieKill.waitFor()
        }

        private fun checkRunApplicationFromIDEA(): Boolean {
            logger.info("bash -c  ps aux | grep -v grep | grep ${TestApp.APP_MAIN_CLASS}")
            val p = ProcessWrapper("ps aux | grep -v grep | grep ${TestApp.APP_MAIN_CLASS}")
            val exitCode = p.waitFor()
            return exitCode == 0
        }


        private fun awaitStartup(url: String, seconds: Long) {
            logger.info("Await startup by polling [$url] for $seconds sec")
            await.timeout(Duration.ofMillis(TimeUnit.SECONDS.toMillis(seconds))).pollInterval(Duration.ofMillis(1000)).until {
                isServiceReady(url)
            }
            if (!isServiceReady(url)) {
                fail("Service unable to start in $seconds s!")
            }
        }

        private fun isServiceReady(url: String): Boolean {
            try {
                val thenReturn = RestAssured.given().log().all()
                    .baseUri(url).get().thenReturn()
                return thenReturn.statusCode == 200
            } catch (ignored: Exception) {
                logger.info { ignored }
            }
            return false
        }
    }
}