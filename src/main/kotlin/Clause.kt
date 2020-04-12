class Clause(private val base: String) {
    val element = mutableListOf<Int>()

    init {
        element.addAll(base.split(" ").map { it.toInt() }.dropLast(1))
    }
}