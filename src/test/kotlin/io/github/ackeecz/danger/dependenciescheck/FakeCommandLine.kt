package io.github.ackeecz.danger.dependenciescheck

internal class FakeCommandLine : CommandLine {

    val executedCommands: MutableList<String> = mutableListOf()

    override fun execute(command: String) {
        executedCommands += command
    }
}
