// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1
private abstract class Base {

    abstract val a: Any
    abstract var b: Any
    internal abstract val c: Any
    internal abstract var d: Any


    abstract fun foo()
    internal abstract fun boo(): Any
}


fun case1() {
    val impl = object : Base() {
        override var a: Any
            get() = TODO()
            set(value) {}
        override val b: Any
            get() = TODO()
        override var c: Any
            get() = TODO()
            set(value) {}
        override val d: Any
            get() = TODO()

        override fun foo() {}

        override fun boo(): Any {
            return ""
        }
    }
}



/*
* TESTCASE NUMBER: 2
* NOTE: property is not implemented
*/
fun case2() {
    val impl = object : Base() {
        override var b: Any
            get() = TODO()
            set(value) {}
        override val c: Any
            get() = TODO()
        override var d: Any
            get() = TODO()
            set(value) {}

        override fun foo() {
            TODO()
        }

        override fun boo(): Any {
            TODO()
        }
    }
}


/*
* TESTCASE NUMBER: 3
* NOTE: function is not implemented
*/

fun case3() {
    val impl = object : Base() {
        override var b: Any
            get() = TODO()
            set(value) {}
        override val c: Any
            get() = TODO()
        override var d: Any
            get() = TODO()
            set(value) {}

        override fun foo() {}

        override fun boo(): Any {
            return 1
        }
    }
}
