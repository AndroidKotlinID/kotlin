/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.declarations

import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.SourceManager
import org.jetbrains.kotlin.ir.symbols.IrExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.symbols.IrFileSymbol
import org.jetbrains.kotlin.ir.symbols.IrPackageFragmentSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import java.io.File

interface IrPackageFragment : IrElement, IrDeclarationContainer, IrSymbolOwner {
    val packageFragmentDescriptor: PackageFragmentDescriptor
    override val symbol: IrPackageFragmentSymbol

    val fqName: FqName
}

interface IrExternalPackageFragment : IrPackageFragment {
    override val symbol: IrExternalPackageFragmentSymbol
    val containerSource: DeserializedContainerSource?
}

interface IrFile : IrPackageFragment, IrMutableAnnotationContainer, IrMetadataSourceOwner {
    override val symbol: IrFileSymbol

    val fileEntry: SourceManager.FileEntry

    override fun <D> transform(transformer: IrElementTransformer<D>, data: D): IrFile =
        accept(transformer, data) as IrFile
}

val IrFile.path: String get() = fileEntry.name
val IrFile.name: String get() = File(path).name
