package cdcl

class Clause(private val base: String) {
    val element = mutableListOf<Int>()
    val now: MutableSet<Int> by lazy { element.toMutableSet() }

    init {
        element.addAll(base.split(" ").map { it.toInt() }.dropLast(1))
    }
}