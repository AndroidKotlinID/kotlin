/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.spring.tests.rename;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("ultimate/testData/spring/core/rename")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class SpringRenameTestGenerated extends AbstractSpringRenameTest {
    public void testAllFilesPresentInRename() throws Exception {
        KotlinTestUtils.assertAllTestsPresentInSingleGeneratedClass(this.getClass(), new File("ultimate/testData/spring/core/rename"), Pattern.compile("^(.+)\\.test$"), TargetBackend.ANY);
    }

    @TestMetadata("annotationArgBySpELRefInXMLConf/annotationArgBySpELRefInXMLConf.test")
    public void testAnnotationArgBySpELRefInXMLConf_AnnotationArgBySpELRefInXMLConf() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/annotationArgBySpELRefInXMLConf/annotationArgBySpELRefInXMLConf.test");
        doTest(fileName);
    }

    @TestMetadata("classWithXmlRefs/classWithXmlRef.test")
    public void testClassWithXmlRefs_ClassWithXmlRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/classWithXmlRefs/classWithXmlRef.test");
        doTest(fileName);
    }

    @TestMetadata("classWithXmlRefsByRef/classWithXmlRefByRef.test")
    public void testClassWithXmlRefsByRef_ClassWithXmlRefByRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/classWithXmlRefsByRef/classWithXmlRefByRef.test");
        doTest(fileName);
    }

    @TestMetadata("factoryMethodParam/factoryMethodParam.test")
    public void testFactoryMethodParam_FactoryMethodParam() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/factoryMethodParam/factoryMethodParam.test");
        doTest(fileName);
    }

    @TestMetadata("factoryMethodParamByXmlRef/factoryMethodParamByXmlRef.test")
    public void testFactoryMethodParamByXmlRef_FactoryMethodParamByXmlRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/factoryMethodParamByXmlRef/factoryMethodParamByXmlRef.test");
        doTest(fileName);
    }

    @TestMetadata("isPropertyWithXmlRefsBySpelRef/isPropertyWithXmlRefBySpelRef.test")
    public void testIsPropertyWithXmlRefsBySpelRef_IsPropertyWithXmlRefBySpelRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/isPropertyWithXmlRefsBySpelRef/isPropertyWithXmlRefBySpelRef.test");
        doTest(fileName);
    }

    @TestMetadata("javaSpelRefToJava/javaSpelRefToJava.test")
    public void testJavaSpelRefToJava_JavaSpelRefToJava() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/javaSpelRefToJava/javaSpelRefToJava.test");
        doTest(fileName);
    }

    @TestMetadata("javaSpelRefToJavaAnnotated/javaSpelRefToJavaAnnotated.test")
    public void testJavaSpelRefToJavaAnnotated_JavaSpelRefToJavaAnnotated() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/javaSpelRefToJavaAnnotated/javaSpelRefToJavaAnnotated.test");
        doTest(fileName);
    }

    @TestMetadata("javaSpelRefToKt/javaSpelRefToKt.test")
    public void testJavaSpelRefToKt_JavaSpelRefToKt() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/javaSpelRefToKt/javaSpelRefToKt.test");
        doTest(fileName);
    }

    @TestMetadata("javaSpelRefToKtAnnotated/javaSpelRefToKtAnnotated.test")
    public void testJavaSpelRefToKtAnnotated_JavaSpelRefToKtAnnotated() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/javaSpelRefToKtAnnotated/javaSpelRefToKtAnnotated.test");
        doTest(fileName);
    }

    @TestMetadata("ktSpelRefToJava/ktSpelRefToJava.test")
    public void testKtSpelRefToJava_KtSpelRefToJava() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/ktSpelRefToJava/ktSpelRefToJava.test");
        doTest(fileName);
    }

    @TestMetadata("ktSpelRefToJavaAnnotated/ktSpelRefToJavaAnnotated.test")
    public void testKtSpelRefToJavaAnnotated_KtSpelRefToJavaAnnotated() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/ktSpelRefToJavaAnnotated/ktSpelRefToJavaAnnotated.test");
        doTest(fileName);
    }

    @TestMetadata("ktSpelRefToKt/ktSpelRefToKt.test")
    public void testKtSpelRefToKt_KtSpelRefToKt() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/ktSpelRefToKt/ktSpelRefToKt.test");
        doTest(fileName);
    }

    @TestMetadata("ktSpelRefToKtAnnotated/ktSpelRefToKtAnnotated.test")
    public void testKtSpelRefToKtAnnotated_KtSpelRefToKtAnnotated() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/ktSpelRefToKtAnnotated/ktSpelRefToKtAnnotated.test");
        doTest(fileName);
    }

    @TestMetadata("parameterWithXmlRefsBySpelRef/parameterWithXmlRefBySpelRef.test")
    public void testParameterWithXmlRefsBySpelRef_ParameterWithXmlRefBySpelRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/parameterWithXmlRefsBySpelRef/parameterWithXmlRefBySpelRef.test");
        doTest(fileName);
    }

    @TestMetadata("primaryConstructorArgWithXmlRefs/primaryConstructorArgWithXmlRef.test")
    public void testPrimaryConstructorArgWithXmlRefs_PrimaryConstructorArgWithXmlRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/primaryConstructorArgWithXmlRefs/primaryConstructorArgWithXmlRef.test");
        doTest(fileName);
    }

    @TestMetadata("primaryConstructorArgWithXmlRefsByRef1/primaryConstructorArgWithXmlRefByRef1.test")
    public void testPrimaryConstructorArgWithXmlRefsByRef1_PrimaryConstructorArgWithXmlRefByRef1() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/primaryConstructorArgWithXmlRefsByRef1/primaryConstructorArgWithXmlRefByRef1.test");
        doTest(fileName);
    }

    @TestMetadata("primaryConstructorArgWithXmlRefsByRef2/primaryConstructorArgWithXmlRefByRef2.test")
    public void testPrimaryConstructorArgWithXmlRefsByRef2_PrimaryConstructorArgWithXmlRefByRef2() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/primaryConstructorArgWithXmlRefsByRef2/primaryConstructorArgWithXmlRefByRef2.test");
        doTest(fileName);
    }

    @TestMetadata("propertyWithXmlRefs/propertyWithXmlRef.test")
    public void testPropertyWithXmlRefs_PropertyWithXmlRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/propertyWithXmlRefs/propertyWithXmlRef.test");
        doTest(fileName);
    }

    @TestMetadata("propertyWithXmlRefsByRef/propertyWithXmlRefByRef.test")
    public void testPropertyWithXmlRefsByRef_PropertyWithXmlRefByRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/propertyWithXmlRefsByRef/propertyWithXmlRefByRef.test");
        doTest(fileName);
    }

    @TestMetadata("propertyWithXmlRefsBySpelRef/propertyWithXmlRefBySpelRef.test")
    public void testPropertyWithXmlRefsBySpelRef_PropertyWithXmlRefBySpelRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/propertyWithXmlRefsBySpelRef/propertyWithXmlRefBySpelRef.test");
        doTest(fileName);
    }

    @TestMetadata("setterFunWithXmlRefs/setterFunWithXmlRef.test")
    public void testSetterFunWithXmlRefs_SetterFunWithXmlRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/setterFunWithXmlRefs/setterFunWithXmlRef.test");
        doTest(fileName);
    }

    @TestMetadata("setterFunWithXmlRefsByRef/setterFunWithXmlRefByRef.test")
    public void testSetterFunWithXmlRefsByRef_SetterFunWithXmlRefByRef() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("ultimate/testData/spring/core/rename/setterFunWithXmlRefsByRef/setterFunWithXmlRefByRef.test");
        doTest(fileName);
    }
}
