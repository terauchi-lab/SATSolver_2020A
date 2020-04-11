class Clause(private val base: String) {
    val literals = mutableListOf<Int>()

    init {
        literals.addAll(base.split(' ').map { it.toInt() }.dropLast(1))
    }
}