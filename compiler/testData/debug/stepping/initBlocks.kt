// FILE: test.kt

class Foo {
    var a: String

    init {
        a = x()
    }
}

class Bar {
    init {
        val a = 5
    }

    init {
        val b = 2
    }
}

class Boo {
    init {
        val a = 5
    }

    val x = x()

    init {
        val b = 2
    }
}

class Zoo {
    init { val a = 5 }

    init { val b = 6 }

    init {
        val c = 7
    }

    init { val d = 8 }
}

fun x() = ""

fun box() {
    Foo()
    Bar()
    Boo()
    Zoo()
}

// IGNORE_BACKEND: JVM_IR
// The JVM_IR does not generate code that will break on the line
// containing the `init`, nor the exit from the `init`. It only
// contains lines for the contents of the init blocks.

// LINENUMBERS
// test.kt:48 box
// test.kt:3 <init>
// test.kt:6 <init>
// test.kt:7 <init>
// test.kt:45 x
// test.kt:7 <init>
// test.kt:8 <init>
// test.kt:48 box
// test.kt:49 box
// test.kt:11 <init>
// test.kt:12 <init>
// test.kt:13 <init>
// test.kt:14 <init>
// test.kt:16 <init>
// test.kt:17 <init>
// test.kt:18 <init>
// test.kt:49 box
// test.kt:50 box
// test.kt:21 <init>
// test.kt:22 <init>
// test.kt:23 <init>
// test.kt:24 <init>
// test.kt:26 <init>
// test.kt:45 x
// test.kt:26 <init>
// test.kt:28 <init>
// test.kt:29 <init>
// test.kt:30 <init>
// test.kt:50 box
// test.kt:51 box
// test.kt:33 <init>
// test.kt:34 <init>
// test.kt:36 <init>
// test.kt:38 <init>
// test.kt:39 <init>
// test.kt:40 <init>
// test.kt:42 <init>
// test.kt:51 box
// test.kt:52 box
