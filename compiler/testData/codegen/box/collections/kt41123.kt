// WITH_RUNTIME
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND_FIR: JVM_IR

open class A : HashMap<String, String>()

fun box(): String {
    val a = object : A() {}
    a["OK"] = "OK"
    return a["OK"]!!
}