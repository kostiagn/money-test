package ru.kostiagn.apitest.util

import mu.KLogging
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Run command in shell and log stdin and stdout of the process with prefix >>>
 *
 * @param cmd - command to run
 */
class ProcessWrapper(cmd: String) {

    companion object : KLogging()

    private val process: Process
    private val executor: ExecutorService

    init {
        logger.info("Starting process wrapper, cmd [$cmd]")
        process = Runtime.getRuntime().exec(arrayOf("/bin/bash", "-c", cmd))
        executor = Executors.newFixedThreadPool(2)
        executor.submit(LoggerRunnable(process.inputStream))
        executor.submit(LoggerRunnable(process.errorStream))
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                logger.info("Calling shutdown hook for $cmd")
                terminate()
            } catch (e: InterruptedException) {
                logger.error("", e)
            }
        })
    }

    /**
     * Terminate process
     */
    fun terminate() {
        logger.info("Stop process")
        executor.shutdownNow()
        process.destroy()
        process.waitFor(15, TimeUnit.SECONDS)
        process.destroyForcibly()
    }

    /**
     * Wait while the process is finished
     *
     * @return the exit value of the process
     */
    fun waitFor(): Int {
        return process.waitFor()
    }

    private class LoggerRunnable(private val stream: InputStream) : Runnable {
        override fun run() {
            try {
                InputStreamReader(stream).use { inputStreamReader ->
                    BufferedReader(inputStreamReader).use { reader ->
                        while (Thread.currentThread().isAlive && !Thread.currentThread().isInterrupted) {
                            val line = reader.readLine()
                            if (line != null) {
                                logger.info(">>> {}", line)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                logger.error("", e)
            }
        }
    }
}
