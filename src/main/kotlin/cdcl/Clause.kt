package cdcl

class Clause(private val base: String) {
    val element = mutableListOf<Int>()
    val now: MutableList<Int> by lazy { element.toMutableList() }

    init {
        element.addAll(base.split(" ").map { it.toInt() }.dropLast(1))
    }
}