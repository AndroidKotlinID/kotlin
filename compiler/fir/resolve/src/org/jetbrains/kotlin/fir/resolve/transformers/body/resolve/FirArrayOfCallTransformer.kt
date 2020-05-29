/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildArrayOfCall
import org.jetbrains.kotlin.fir.resolve.transformers.getOriginalFunction
import org.jetbrains.kotlin.fir.types.isArrayType

/**
 * A transformer that converts resolved arrayOf() call to [FirArrayOfCall].
 *
 * Note that arrayOf() calls only in [FirAnnotationCall] or the default value of annotation constructor are transformed.
 */
internal class FirArrayOfCallTransformer {
    private val FirFunctionCall.isArrayOfCall: Boolean
        get() {
            val function: FirCallableDeclaration<*> = getOriginalFunction() ?: return false
            return function is FirSimpleFunction &&
                    // TODO: extend it to other variants, like emptyArray, intArrayOf, floatArrayOf, etc.
                    function.name.asString() == "arrayOf" &&
                    function.returnTypeRef.isArrayType &&
                    function.valueParameters.size == 1 && function.valueParameters[0].isVararg &&
                    arguments.size == 1 &&
                    function.receiverTypeRef == null
        }

    internal fun toArrayOfCall(functionCall: FirFunctionCall): FirArrayOfCall? {
        if (!functionCall.isArrayOfCall) {
            return null
        }
        val typeRef = functionCall.typeRef
        return buildArrayOfCall {
            source = functionCall.source
            annotations += functionCall.annotations
            // Note that the signature is: arrayOf(vararg element). Hence, unwrapping the original argument list here.
            argumentList = buildArgumentList {
                if (functionCall.arguments.isNotEmpty()) {
                    (functionCall.argument as FirVarargArgumentsExpression).arguments.forEach {
                        arguments += it
                    }
                }
            }
        }.apply {
            replaceTypeRef(typeRef)
        }
    }
}
