package io.github.ackeecz.danger.dependenciescheck

import kotlin.concurrent.thread
import kotlin.system.exitProcess

internal interface CommandLine {

    fun execute(command: String)
}

internal class CommandLineImpl : CommandLine {

    override fun execute(command: String) {
        val process = Runtime.getRuntime().exec(arrayOf("/bin/bash", "-c", command))
        thread {
            process.inputStream.bufferedReader()
                .lineSequence()
                .forEach(::println)
        }
        thread {
            process.errorStream.bufferedReader()
                .lineSequence()
                .forEach(System.err::println)
        }
        process.waitFor()
        process.failJobOnError()
    }

    // TODO There might be a better way of how to fail the Danger when some Gradle task or other command
    //  fails, but not sure now
    private fun Process.failJobOnError() {
        val exitCode = exitValue()
        if (exitCode == 1) {
            exitProcess(exitCode)
        }
    }
}
