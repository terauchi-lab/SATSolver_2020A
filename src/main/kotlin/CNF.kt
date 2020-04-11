class CNF(private val base: List<String>) {
    val clauses = mutableListOf<Clause>()

    init {
        base.forEach {
            clauses.add(Clause(it))
        }
    }
}