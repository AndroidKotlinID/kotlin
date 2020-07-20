/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.tests

import java.io.File
import java.io.PrintWriter

object GenerateSteppedRangesCodegenTestData {
    private val TEST_DATA_DIR = File("compiler/testData/codegen/box/ranges/stepped")
    private val UNSIGNED_TEST_DATA_DIR = File(TEST_DATA_DIR, "unsigned")

    private val PREAMBLE_MESSAGE = "Auto-generated by ${GenerateSteppedRangesCodegenTestData::class.java.simpleName}. Do not edit!"
    private val KT_34166_HEADER = """
        |// KT-34166: Translation of loop over literal completely removes the validation of step
        |// DONT_TARGET_EXACT_BACKEND: JS""".trimMargin()
    private val KT_34166_AFFECTED_FILENAMES = setOf("illegalStepZero.kt", "illegalStepNegative.kt", "illegalStepNonConst.kt")

    private val JVM_IR_FAILING_FOR_UNSIGNED_FILENAMES = setOf<String>()

    private enum class Type(val type: String, val isLong: Boolean = false, val isUnsigned: Boolean = false) {
        INT("Int"),
        LONG("Long", isLong = true),
        CHAR("Char") {
            override fun convertIntValue(i: Int, forStep: Boolean) = "'${'a' - 1 + i}'"
        },
        UINT("UInt", isUnsigned = true) {
            // Step is unsigned for UIntProgression.
            override fun convertIntValue(i: Int, forStep: Boolean) = if (forStep) INT.convertValue(i) else "${i}u"
        },
        ULONG("ULong", isUnsigned = true, isLong = true) {
            // Step is unsigned for ULongProgression.
            override fun convertIntValue(i: Int, forStep: Boolean) = if (forStep) LONG.convertValue(i) else "${i}uL"
        };

        protected open fun convertIntValue(i: Int, forStep: Boolean): String =
            i.toString() + (if (isUnsigned) "u" else "") + (if (isLong) "L" else "")

        fun convertValue(i: Any, forStep: Boolean = false) = when (i) {
            is Int -> convertIntValue(i, forStep)
            // For convenience, replace the type in value expressions so we can re-use the same builder for different types
            else -> i.toString().replace("U?Int\\.".toRegex(), "$type.")
        }
    }

    private enum class Function(val infixFunctionName: String, val subdir: String) {
        RANGE_TO("..", "rangeTo"),
        UNTIL(" until ", "until"),
        DOWN_TO(" downTo ", "downTo")
    }

    private class TestBuilder(
        firstLast: Pair<Any, Any>,
        val operations: MutableList<Operation> = mutableListOf(),
        expectedValuesOrFailIfNull: List<Any>? = null
    ) {
        val first = firstLast.first
        val last = firstLast.second
        var expectedValuesOrFailIfNull: List<Any>? = expectedValuesOrFailIfNull
            private set

        private abstract class Operation
        private class Step(val step: Int) : Operation()
        private class StepExpression(val expression: String) : Operation()
        private object Reversed : Operation()

        fun copy(first: Any = this.first, last: Any = this.last, expectedValuesOrFailIfNull: List<Any>? = this.expectedValuesOrFailIfNull) =
            TestBuilder(first to last, operations, expectedValuesOrFailIfNull)

        fun step(step: Int) = this.also { operations += Step(step) }
        fun step(expression: String) = this.also { operations += StepExpression(expression) }
        fun reversed() = this.also { operations += Reversed }
        fun expectValues(vararg expectedValues: Any) = this.also { expectedValuesOrFailIfNull = listOf(*expectedValues) }
        fun shouldFail() = this.also { expectedValuesOrFailIfNull = null }

        fun buildOnTopOfRangeOnlyVariable(variable: String, type: Type) =
            StringBuilder(variable).appendOperations(type, shouldParenthesizeFirst = true).toString()

        fun buildFullLiteral(type: Type, function: Function) =
            StringBuilder(buildRangeOnlyExpression(type, function)).appendOperations(type).toString()

        fun buildRangeOnlyExpression(type: Type, function: Function) =
            type.convertValue(first) + function.infixFunctionName + type.convertValue(last)

        private fun StringBuilder.appendOperations(type: Type, shouldParenthesizeFirst: Boolean = false) = this.also {
            for ((index, op) in operations.withIndex()) {
                when (op) {
                    is Step -> {
                        append(" step ").append(op.step)
                        if (type.isLong) {
                            append('L')
                        }
                    }
                    is StepExpression -> {
                        append(" step ").append(op.expression)
                    }
                    Reversed -> {
                        if (!shouldParenthesizeFirst || index > 0) {
                            insert(0, '(')
                            append(')')
                        }
                        append(".reversed()")
                    }
                }
            }
        }
    }

    private fun generateTests(
        fileName: String,
        rangeToBuilder: TestBuilder,
        extraCode: String? = null,
        subdir: String? = null
    ) {
        generateTestsForRangeToAndUntil(fileName, rangeToBuilder, extraCode, subdir)
        // For convenience, derive test for "downTo" by swapping "first"/"last" and reversing expected values
        generateTestsForFunction(
            fileName,
            rangeToBuilder.copy(
                first = rangeToBuilder.last,
                last = rangeToBuilder.first,
                expectedValuesOrFailIfNull = rangeToBuilder.expectedValuesOrFailIfNull?.asReversed()
            ),
            Function.DOWN_TO,
            extraCode,
            subdir
        )
    }

    private fun generateTestsForRangeToAndUntil(
        fileName: String,
        rangeToBuilder: TestBuilder,
        extraCode: String? = null,
        subdir: String? = null
    ) {
        generateTestsForFunction(fileName, rangeToBuilder, Function.RANGE_TO, extraCode, subdir)
        // For convenience, derive test for "until" by incrementing "last"
        generateTestsForFunction(fileName, rangeToBuilder.copy(last = (rangeToBuilder.last as Int) + 1), Function.UNTIL, extraCode, subdir)
    }

    private fun generateTestsForFunction(
        fileName: String,
        builder: TestBuilder,
        function: Function,
        extraCode: String? = null,
        subdir: String? = null
    ) = generateTestsForFunction(fileName, Type.values().associate { it to builder }, function, extraCode, subdir)

    private fun generateTestsForFunction(
        fileName: String,
        typeToBuilderMap: Map<Type, TestBuilder>,
        function: Function,
        extraCode: String? = null,
        subdir: String? = null
    ) = listOf(true, false).forEach { generateTestsForFunction(fileName, typeToBuilderMap, function, extraCode, subdir, asLiteral = it) }

    private fun generateTestsForFunction(
        fileName: String,
        typeToBuilderMap: Map<Type, TestBuilder>,
        function: Function,
        extraCode: String?,
        fullSubdir: File,
        asLiteral: Boolean,
        shouldIgnoreForJvmIR: Boolean = false
    ) {
        fullSubdir.mkdirs()
        PrintWriter(File(fullSubdir, fileName)).use {
            with(it) {
                println("// $PREAMBLE_MESSAGE")
                if (shouldIgnoreForJvmIR) {
                    println("// IGNORE_BACKEND: JVM_IR")
                }
                println("// KJS_WITH_FULL_RUNTIME")
                println("// WITH_RUNTIME")
                if (asLiteral && KT_34166_AFFECTED_FILENAMES.contains(fileName)) {
                    println(KT_34166_HEADER)
                }
                println("import kotlin.test.*")
                println()

                if (extraCode != null) {
                    println(extraCode)
                    println()
                }
                println("fun box(): String {")
                for ((type, builder) in typeToBuilderMap) {
                    printTestForFunctionAndType(builder, function, type, asLiteral)
                }
                println("    return \"OK\"")
                print("}")
            }
        }
    }

    private fun generateTestsForFunction(
        fileName: String,
        typeToBuilderMap: Map<Type, TestBuilder>,
        function: Function,
        extraCode: String? = null,
        subdir: String? = null,
        asLiteral: Boolean
    ) {
        val fullSubdirPath = buildString {
            if (asLiteral) append("literal") else append("expression")
            append("/").append(function.subdir)
            if (subdir != null) {
                append("/").append(subdir)
            }
        }
        val (unsignedTests, signedTests) = typeToBuilderMap.asSequence().partition { (type, _) -> type.isUnsigned }
        if (unsignedTests.isNotEmpty()) {
            generateTestsForFunction(
                fileName,
                unsignedTests.associate { it.toPair() },
                function,
                extraCode,
                File(UNSIGNED_TEST_DATA_DIR, fullSubdirPath),
                asLiteral,
                shouldIgnoreForJvmIR = fileName in JVM_IR_FAILING_FOR_UNSIGNED_FILENAMES,
            )
        }
        if (signedTests.isNotEmpty()) {
            generateTestsForFunction(
                fileName,
                signedTests.associate { it.toPair() },
                function,
                extraCode,
                File(TEST_DATA_DIR, fullSubdirPath),
                asLiteral
            )
        }
    }

    private fun PrintWriter.printTestForFunctionAndType(builder: TestBuilder, function: Function, type: Type, asLiteral: Boolean) {
        val shouldFail = (builder.expectedValuesOrFailIfNull == null)
        val listVarName = type.type.toLowerCase() + "List"
        if (shouldFail) {
            println("    assertFailsWith<IllegalArgumentException> {")
        } else {
            println("    val $listVarName = mutableListOf<${type.type}>()")
        }

        val indent = if (shouldFail) "    " else ""
        if (asLiteral) {
            println("$indent    for (i in ${builder.buildFullLiteral(type, function)}) {")
        } else {
            val progressionVarName = type.type.toLowerCase() + "Progression"
            println("$indent    val $progressionVarName = ${builder.buildRangeOnlyExpression(type, function)}")
            println("$indent    for (i in ${builder.buildOnTopOfRangeOnlyVariable(progressionVarName, type)}) {")
        }
        if (!shouldFail) {
            println("        $listVarName += i")
        }
        println("$indent    }")

        if (!shouldFail) {
            if (builder.expectedValuesOrFailIfNull!!.isEmpty()) {
                println("    assertTrue($listVarName.isEmpty())")
            } else {
                println("    assertEquals(listOf(${builder.expectedValuesOrFailIfNull!!.joinToString { type.convertValue(it) }}), $listVarName)")
            }
        } else {
            println("    }")
        }
        println()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        generateTests("emptyProgression.kt", TestBuilder(2 to 1).step(2).expectValues())
        generateTests("illegalStepNegative.kt", TestBuilder(1 to 7).step(-1).shouldFail())
        generateTests("illegalStepThenLegalStep.kt", TestBuilder(1 to 7).step(0).step(2).shouldFail())
        generateTests("illegalStepZero.kt", TestBuilder(1 to 7).step(0).shouldFail())
        generateTests("legalStepThenIllegalStep.kt", TestBuilder(1 to 7).step(2).step(0).shouldFail())
        generateTests("singleElementStepTwo.kt", TestBuilder(1 to 1).step(2).expectValues(1))
        generateTests("stepOne.kt", TestBuilder(1 to 4).step(1).expectValues(1, 2, 3, 4))
        generateTests("stepToSameLast.kt", TestBuilder(1 to 7).step(2).expectValues(1, 3, 5, 7))
        generateTestsForRangeToAndUntil("stepToOutsideRange.kt", TestBuilder(1 to 7).step(7).expectValues(1))
        generateTestsForFunction("stepToOutsideRange.kt", TestBuilder(7 to 1).step(7).expectValues(7), Function.DOWN_TO)
        generateTestsForRangeToAndUntil("stepToSmallerLast.kt", TestBuilder(1 to 8).step(2).expectValues(1, 3, 5, 7))
        generateTestsForFunction("stepToSmallerLast.kt", TestBuilder(8 to 1).step(2).expectValues(8, 6, 4, 2), Function.DOWN_TO)

        // Nested step cases
        generateTests("stepOneThenStepOne.kt", TestBuilder(1 to 4).step(1).step(1).expectValues(1, 2, 3, 4), subdir = "nestedStep")
        generateTests(
            "stepToSameLastThenStepOne.kt",
            TestBuilder(1 to 5).step(2).step(1).expectValues(1, 2, 3, 4, 5),
            subdir = "nestedStep"
        )
        generateTests(
            "stepToSameLastThenStepToSameLast.kt",
            TestBuilder(1 to 7).step(3).step(2).expectValues(1, 3, 5, 7),
            subdir = "nestedStep"
        )
        generateTestsForRangeToAndUntil(
            "stepThenSameStep.kt",
            TestBuilder(1 to 8).step(2).step(2).expectValues(1, 3, 5, 7),
            subdir = "nestedStep"
        )
        generateTestsForFunction(
            "stepThenSameStep.kt",
            TestBuilder(8 to 1).step(2).step(2).expectValues(8, 6, 4, 2),
            Function.DOWN_TO,
            subdir = "nestedStep"
        )
        generateTestsForRangeToAndUntil(
            "stepToSameLastThenStepToSmallerLast.kt",
            TestBuilder(1 to 10).step(3).step(2).expectValues(1, 3, 5, 7, 9),
            subdir = "nestedStep"
        )
        generateTestsForFunction(
            "stepToSameLastThenStepToSmallerLast.kt",
            TestBuilder(10 to 1).step(3).step(2).expectValues(10, 8, 6, 4, 2),
            Function.DOWN_TO,
            subdir = "nestedStep"
        )
        generateTestsForRangeToAndUntil(
            "stepToSmallerLastThenStepOne.kt",
            TestBuilder(1 to 6).step(2).step(1).expectValues(1, 2, 3, 4, 5),
            subdir = "nestedStep"
        )
        generateTestsForFunction(
            "stepToSmallerLastThenStepOne.kt",
            TestBuilder(6 to 1).step(2).step(1).expectValues(6, 5, 4, 3, 2),
            Function.DOWN_TO,
            subdir = "nestedStep"
        )
        generateTestsForRangeToAndUntil(
            "stepToSmallerLastThenStepToSameLast.kt",
            TestBuilder(1 to 8).step(2).step(3).expectValues(1, 4, 7),
            subdir = "nestedStep"
        )
        generateTestsForFunction(
            "stepToSmallerLastThenStepToSameLast.kt",
            TestBuilder(8 to 1).step(2).step(3).expectValues(8, 5, 2),
            Function.DOWN_TO,
            subdir = "nestedStep"
        )
        generateTestsForRangeToAndUntil(
            "stepToSmallerLastThenStepToSmallerLast.kt",
            TestBuilder(1 to 10).step(2).step(3).expectValues(1, 4, 7),
            subdir = "nestedStep"
        )
        generateTestsForFunction(
            "stepToSmallerLastThenStepToSmallerLast.kt",
            TestBuilder(10 to 1).step(2).step(3).expectValues(10, 7, 4),
            Function.DOWN_TO,
            subdir = "nestedStep"
        )

        // Reversed cases
        generateTestsForRangeToAndUntil(
            "reversedThenStep.kt",
            TestBuilder(1 to 8).reversed().step(2).expectValues(8, 6, 4, 2),
            subdir = "reversed"
        )
        generateTestsForFunction(
            "reversedThenStep.kt",
            TestBuilder(8 to 1).reversed().step(2).expectValues(1, 3, 5, 7),
            Function.DOWN_TO,
            subdir = "reversed"
        )
        generateTestsForRangeToAndUntil(
            "reversedThenStepThenReversed.kt",
            TestBuilder(1 to 8).reversed().step(2).reversed().expectValues(2, 4, 6, 8),
            subdir = "reversed"
        )
        generateTestsForFunction(
            "reversedThenStepThenReversed.kt",
            TestBuilder(8 to 1).reversed().step(2).reversed().expectValues(7, 5, 3, 1),
            Function.DOWN_TO,
            subdir = "reversed"
        )
        generateTestsForRangeToAndUntil(
            "reversedThenStepThenReversedThenStep.kt",
            TestBuilder(1 to 10).reversed().step(2).reversed().step(3).expectValues(2, 5, 8),
            subdir = "reversed"
        )
        generateTestsForFunction(
            "reversedThenStepThenReversedThenStep.kt",
            TestBuilder(10 to 1).reversed().step(2).reversed().step(3).expectValues(9, 6, 3),
            Function.DOWN_TO,
            subdir = "reversed"
        )
        generateTestsForRangeToAndUntil(
            "stepThenReversed.kt",
            TestBuilder(1 to 8).step(2).reversed().expectValues(7, 5, 3, 1),
            subdir = "reversed"
        )
        generateTestsForFunction(
            "stepThenReversed.kt",
            TestBuilder(8 to 1).step(2).reversed().expectValues(2, 4, 6, 8),
            Function.DOWN_TO,
            subdir = "reversed"
        )
        generateTestsForRangeToAndUntil(
            "stepThenReversedThenStep.kt",
            TestBuilder(1 to 10).step(2).reversed().step(3).expectValues(9, 6, 3),
            subdir = "reversed"
        )
        generateTestsForFunction(
            "stepThenReversedThenStep.kt",
            TestBuilder(10 to 1).step(2).reversed().step(3).expectValues(2, 5, 8),
            Function.DOWN_TO,
            subdir = "reversed"
        )
        generateTestsForRangeToAndUntil(
            "stepThenReversedThenStepThenReversed.kt",
            TestBuilder(1 to 10).step(2).reversed().step(3).reversed().expectValues(3, 6, 9),
            subdir = "reversed"
        )
        generateTestsForFunction(
            "stepThenReversedThenStepThenReversed.kt",
            TestBuilder(10 to 1).step(2).reversed().step(3).reversed().expectValues(8, 5, 2),
            Function.DOWN_TO,
            subdir = "reversed"
        )

        // Tests with different expectations for each function and/or type
        generateTestsForFunction("emptyProgressionToMinValue.kt", TestBuilder(2 to "Int.MIN_VALUE").step(2).expectValues(), Function.UNTIL)
        generateTestsForFunction(
            "progressionToNonConst.kt",
            mapOf(
                Type.INT to TestBuilder(1 to "nine()").step(2).expectValues(1, 3, 5, 7),
                Type.LONG to TestBuilder(1 to "nine().toLong()").step(2).expectValues(1, 3, 5, 7),
                Type.CHAR to TestBuilder("'a'" to "('a' - 1 + nine())").step(2).expectValues("'a'", "'c'", "'e'", "'g'"),
                Type.UINT to TestBuilder(1 to "nine().toUInt()").step(2).expectValues(1, 3, 5, 7),
                Type.ULONG to TestBuilder(1 to "nine().toULong()").step(2).expectValues(1, 3, 5, 7)
            ),
            Function.UNTIL,
            extraCode = "fun nine() = 9"
        )
        listOf(Function.RANGE_TO, Function.UNTIL).forEach { function ->
            generateTestsForFunction(
                "illegalStepNonConst.kt",
                TestBuilder(1 to 7).step("zero()").shouldFail().let {
                    mapOf(
                        // Argument for (U)LongProgression.step() must be a Long
                        Type.INT to it,
                        Type.LONG to TestBuilder(1 to 7).step("zero().toLong()").shouldFail(),
                        Type.CHAR to it,
                        Type.UINT to it,
                        Type.ULONG to TestBuilder(1 to 7).step("zero().toLong()").shouldFail()
                    )
                },
                function,
                extraCode = "fun zero() = 0"
            )
        }
        generateTestsForFunction(
            "illegalStepNonConst.kt",
            TestBuilder(7 to 1).step("zero()").shouldFail().let {
                mapOf(
                    // Argument for (U)LongProgression.step() must be a Long
                    Type.INT to it,
                    Type.LONG to TestBuilder(7 to 1).step("zero().toLong()").shouldFail(),
                    Type.CHAR to it,
                    Type.UINT to it,
                    Type.ULONG to TestBuilder(7 to 1).step("zero().toLong()").shouldFail(),
                )
            },
            Function.DOWN_TO,
            extraCode = "fun zero() = 0"
        )
        generateTestsForFunction(
            "stepNonConst.kt",
            TestBuilder(1 to 8).step("two()").expectValues(1, 3, 5, 7).let {
                mapOf(
                    // Argument for (U)LongProgression.step() must be a Long
                    Type.INT to it,
                    Type.LONG to TestBuilder(1 to 8).step("two().toLong()").expectValues(1, 3, 5, 7),
                    Type.CHAR to it,
                    Type.UINT to it,
                    Type.ULONG to TestBuilder(1 to 8).step("two().toLong()").expectValues(1, 3, 5, 7),
                )
            },
            Function.RANGE_TO,
            extraCode = "fun two() = 2"
        )
        generateTestsForFunction(
            "stepNonConst.kt",
            TestBuilder(1 to 9).step("two()").expectValues(1, 3, 5, 7).let {
                mapOf(
                    // Argument for (U)LongProgression.step() must be a Long
                    Type.INT to it,
                    Type.LONG to TestBuilder(1 to 9).step("two().toLong()").expectValues(1, 3, 5, 7),
                    Type.CHAR to it,
                    Type.UINT to it,
                    Type.ULONG to TestBuilder(1 to 9).step("two().toLong()").expectValues(1, 3, 5, 7),
                )
            },
            Function.UNTIL,
            extraCode = "fun two() = 2"
        )
        generateTestsForFunction(
            "stepNonConst.kt",
            TestBuilder(8 to 1).step("two()").expectValues(8, 6, 4, 2).let {
                mapOf(
                    // Argument for (U)LongProgression.step() must be a Long
                    Type.INT to it,
                    Type.LONG to TestBuilder(8 to 1).step("two().toLong()").expectValues(8, 6, 4, 2),
                    Type.CHAR to it,
                    Type.UINT to it,
                    Type.ULONG to TestBuilder(8 to 1).step("two().toLong()").expectValues(8, 6, 4, 2)
                )
            },
            Function.DOWN_TO,
            extraCode = "fun two() = 2"
        )
        generateTestsForFunction(
            "mixedTypeStep.kt",
            mapOf(
                Type.INT to TestBuilder("1.toShort()" to "7.toByte()").step(2).expectValues(1, 3, 5, 7),
                Type.LONG to TestBuilder("1L" to "7").step("2").expectValues(1, 3, 5, 7),
                Type.UINT to TestBuilder("1.toUByte()" to "7.toUByte()").step(2).expectValues(1, 3, 5, 7),
            ),
            Function.RANGE_TO
        )
        generateTestsForFunction(
            "mixedTypeStep.kt",
            mapOf(
                Type.INT to TestBuilder("1.toShort()" to "8.toByte()").step(2).expectValues(1, 3, 5, 7),
                Type.LONG to TestBuilder("1L" to "8").step("2").expectValues(1, 3, 5, 7),
                Type.UINT to TestBuilder("1.toUByte()" to "8.toUByte()").step(2).expectValues(1, 3, 5, 7),
            ),
            Function.UNTIL
        )
        generateTestsForFunction(
            "mixedTypeStep.kt",
            mapOf(
                Type.INT to TestBuilder("7.toByte()" to "1.toShort()").step(2).expectValues(7, 5, 3, 1),
                Type.LONG to TestBuilder("7" to "1L").step("2").expectValues(7, 5, 3, 1),
                Type.UINT to TestBuilder("7.toUByte()" to "1.toUByte()").step(2).expectValues(7, 5, 3, 1),
            ),
            Function.DOWN_TO
        )
        generateTestsForFunction(
            "minValueToMaxValueStepMaxValue.kt",
            mapOf(
                Type.INT to TestBuilder("Int.MIN_VALUE" to "Int.MAX_VALUE").step("Int.MAX_VALUE").expectValues(
                    "Int.MIN_VALUE",
                    -1,
                    "Int.MAX_VALUE - 1"
                ),
                Type.LONG to TestBuilder("Long.MIN_VALUE" to "Long.MAX_VALUE").step("Long.MAX_VALUE").expectValues(
                    "Long.MIN_VALUE",
                    -1,
                    "Long.MAX_VALUE - 1"
                ),
                Type.CHAR to TestBuilder(
                    "Char.MIN_VALUE" to "Char.MAX_VALUE"
                ).step("Char.MAX_VALUE.toInt()").expectValues("Char.MIN_VALUE", "Char.MAX_VALUE"),
                Type.UINT to TestBuilder("UInt.MIN_VALUE" to "UInt.MAX_VALUE").step("Int.MAX_VALUE").expectValues(
                    "UInt.MIN_VALUE",
                    "2147483647u",
                    "UInt.MAX_VALUE - 1u"
                ),
                Type.ULONG to TestBuilder("ULong.MIN_VALUE" to "ULong.MAX_VALUE").step("Long.MAX_VALUE").expectValues(
                    "ULong.MIN_VALUE",
                    "9223372036854775807uL",
                    "ULong.MAX_VALUE - 1uL"
                )
            ),
            Function.RANGE_TO
        )
        generateTestsForFunction(
            "minValueToMaxValueStepMaxValue.kt",
            mapOf(
                Type.INT to TestBuilder("Int.MIN_VALUE" to "Int.MAX_VALUE").step("Int.MAX_VALUE").expectValues(
                    "Int.MIN_VALUE",
                    -1,
                    "Int.MAX_VALUE - 1"
                ),
                Type.LONG to TestBuilder("Long.MIN_VALUE" to "Long.MAX_VALUE").step("Long.MAX_VALUE").expectValues(
                    "Long.MIN_VALUE",
                    -1,
                    "Long.MAX_VALUE - 1"
                ),
                Type.UINT to TestBuilder("UInt.MIN_VALUE" to "UInt.MAX_VALUE").step("Int.MAX_VALUE").expectValues(
                    "UInt.MIN_VALUE",
                    "2147483647u",
                    "UInt.MAX_VALUE - 1u"
                ),
                Type.ULONG to TestBuilder("ULong.MIN_VALUE" to "ULong.MAX_VALUE").step("Long.MAX_VALUE").expectValues(
                    "ULong.MIN_VALUE",
                    "9223372036854775807uL",
                    "ULong.MAX_VALUE - 1uL"
                )
            ),
            Function.UNTIL
        )
        generateTestsForFunction(
            "oneToMaxValueStepMaxValue.kt",
            mapOf(
                Type.INT to TestBuilder(1 to "Int.MAX_VALUE").step("Int.MAX_VALUE").expectValues(1),
                Type.LONG to TestBuilder(1 to "Long.MAX_VALUE").step("Long.MAX_VALUE").expectValues(1),
                Type.CHAR to TestBuilder("1.toChar()" to "Char.MAX_VALUE").step("Char.MAX_VALUE.toInt()").expectValues("1.toChar()"),
                Type.UINT to TestBuilder(1 to "UInt.MAX_VALUE").step("Int.MAX_VALUE").expectValues(
                    1, "2147483648u",
                    "UInt.MAX_VALUE"
                ),
                Type.ULONG to TestBuilder(1 to "ULong.MAX_VALUE").step("Long.MAX_VALUE").expectValues(
                    1, "9223372036854775808uL",
                    "ULong.MAX_VALUE"
                ),
            ),
            Function.RANGE_TO
        )
        generateTestsForFunction(
            "zeroToMaxValueStepMaxValue.kt",
            mapOf(
                Type.INT to TestBuilder(0 to "Int.MAX_VALUE").step("Int.MAX_VALUE").expectValues(0, "Int.MAX_VALUE"),
                Type.LONG to TestBuilder(0 to "Long.MAX_VALUE").step("Long.MAX_VALUE").expectValues(0, "Long.MAX_VALUE"),
                Type.CHAR to TestBuilder("0.toChar()" to "Char.MAX_VALUE").step("Char.MAX_VALUE.toInt()").expectValues(
                    "0.toChar()",
                    "Char.MAX_VALUE"
                ),
                Type.UINT to TestBuilder(0 to "UInt.MAX_VALUE").step("Int.MAX_VALUE").expectValues(0, "2147483647u", "UInt.MAX_VALUE - 1u"),
                Type.ULONG to TestBuilder(0 to "ULong.MAX_VALUE").step("Long.MAX_VALUE")
                    .expectValues(0, "9223372036854775807uL", "ULong.MAX_VALUE - 1uL")
            ),
            Function.RANGE_TO
        )
        generateTestsForFunction(
            "zeroToMaxValueStepMaxValue.kt",
            mapOf(
                Type.INT to TestBuilder(0 to "Int.MAX_VALUE").step("Int.MAX_VALUE").expectValues(0),
                Type.LONG to TestBuilder(0 to "Long.MAX_VALUE").step("Long.MAX_VALUE").expectValues(0),
                Type.CHAR to TestBuilder("0.toChar()" to "Char.MAX_VALUE").step("Char.MAX_VALUE.toInt()").expectValues("0.toChar()"),
                Type.UINT to TestBuilder(0 to "UInt.MAX_VALUE").step("Int.MAX_VALUE").expectValues(0, "2147483647u", "UInt.MAX_VALUE - 1u"),
                Type.ULONG to TestBuilder(0 to "ULong.MAX_VALUE").step("Long.MAX_VALUE")
                    .expectValues(0, "9223372036854775807uL", "ULong.MAX_VALUE - 1uL")
            ),
            Function.UNTIL
        )
        generateTestsForFunction(
            "maxValueToMinValueStepMaxValue.kt",
            mapOf(
                Type.INT to TestBuilder("Int.MAX_VALUE" to "Int.MIN_VALUE").step("Int.MAX_VALUE").expectValues(
                    "Int.MAX_VALUE",
                    0,
                    "Int.MIN_VALUE + 1"
                ),
                Type.LONG to TestBuilder("Long.MAX_VALUE" to "Long.MIN_VALUE").step("Long.MAX_VALUE").expectValues(
                    "Long.MAX_VALUE",
                    0,
                    "Long.MIN_VALUE + 1"
                ),
                Type.CHAR to TestBuilder("Char.MAX_VALUE" to "Char.MIN_VALUE").step("Char.MAX_VALUE.toInt()").expectValues(
                    "Char.MAX_VALUE",
                    "Char.MIN_VALUE"
                ),
                Type.UINT to TestBuilder("UInt.MAX_VALUE" to "UInt.MIN_VALUE").step("Int.MAX_VALUE").expectValues(
                    "UInt.MAX_VALUE",
                    "2147483648u",
                    1
                ),
                Type.ULONG to TestBuilder("ULong.MAX_VALUE" to "ULong.MIN_VALUE").step("Long.MAX_VALUE").expectValues(
                    "ULong.MAX_VALUE",
                    "9223372036854775808uL",
                    1
                )
            ),
            Function.DOWN_TO
        )
        generateTestsForFunction(
            "maxValueToOneStepMaxValue.kt",
            mapOf(
                Type.INT to TestBuilder("Int.MAX_VALUE" to 1).step("Int.MAX_VALUE").expectValues("Int.MAX_VALUE"),
                Type.LONG to TestBuilder("Long.MAX_VALUE" to 1).step("Long.MAX_VALUE").expectValues("Long.MAX_VALUE"),
                Type.CHAR to TestBuilder("Char.MAX_VALUE" to "1.toChar()").step("Char.MAX_VALUE.toInt()").expectValues("Char.MAX_VALUE"),
                Type.UINT to TestBuilder("UInt.MAX_VALUE" to 1).step("Int.MAX_VALUE").expectValues("UInt.MAX_VALUE", "2147483648u", 1),
                Type.ULONG to TestBuilder("ULong.MAX_VALUE" to 1).step("Long.MAX_VALUE")
                    .expectValues("ULong.MAX_VALUE", "9223372036854775808uL", 1),
            ),
            Function.DOWN_TO
        )
        generateTestsForFunction(
            "maxValueToZeroStepMaxValue.kt",
            mapOf(
                Type.INT to TestBuilder("Int.MAX_VALUE" to 0).step("Int.MAX_VALUE").expectValues("Int.MAX_VALUE", 0),
                Type.LONG to TestBuilder("Long.MAX_VALUE" to 0).step("Long.MAX_VALUE").expectValues("Long.MAX_VALUE", 0),
                Type.CHAR to TestBuilder("Char.MAX_VALUE" to "0.toChar()").step("Char.MAX_VALUE.toInt()").expectValues(
                    "Char.MAX_VALUE",
                    "0.toChar()"
                ),
                Type.UINT to TestBuilder("UInt.MAX_VALUE" to 0).step("Int.MAX_VALUE")
                    .expectValues("UInt.MAX_VALUE", "2147483648u", 1),
                Type.ULONG to TestBuilder("ULong.MAX_VALUE" to 0).step("Long.MAX_VALUE")
                    .expectValues("ULong.MAX_VALUE", "9223372036854775808uL", 1)
            ),
            Function.DOWN_TO
        )
    }
}