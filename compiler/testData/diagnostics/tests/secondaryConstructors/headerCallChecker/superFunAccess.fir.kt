// !DIAGNOSTICS: -UNUSED_PARAMETER
open class B(x: Int) {
    fun foo() = 1
}
class A : B {
    constructor(x: Int, y: Int = x <!AMBIGUITY!>+<!> <!UNRESOLVED_REFERENCE!>foo<!>() + this.<!UNRESOLVED_REFERENCE!>foo<!>() + super.<!UNRESOLVED_REFERENCE!>foo<!>()) :
        super(x + foo() + this.foo() + super.foo())
}
