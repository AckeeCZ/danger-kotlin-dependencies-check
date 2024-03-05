package io.github.ackeecz.danger.dependenciescheck.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class TaskNameWithArgsTest : FunSpec({

    test("create task name without args") {
        val expectedTaskName = "dependencyUpdates"

        val actual = TaskNameWithArgs(taskName = expectedTaskName, args = emptyList())

        actual.value shouldBe expectedTaskName
    }

    test("create task name with multiple arguments") {
        val taskName = "dependencyCheckAnalyze"
        val firstArg = "--first"
        val secondArg = "--second"

        val actual = TaskNameWithArgs(taskName = taskName, args = listOf(firstArg, secondArg))

        actual.value shouldBe "$taskName $firstArg $secondArg"
    }
})
