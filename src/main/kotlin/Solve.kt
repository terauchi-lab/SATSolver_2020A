import java.io.File

class Solve(private val file: File) {
    fun run() {
        val texts = file.readLines()

        val size = texts[0].split(' ')[2].toInt()
        val clause = mutableListOf<Clause>()
        texts.drop(1).forEach {
            clause.add(Clause(it))
        }
        val literal = mutableListOf<Pair<Int, Boolean>>()
        repeat(size) {
            literal.add(Pair(it + 1, true))
        }

        val cnf = CNF(size, clause, literal)
        cnf.clauses.forEach {
            println(it.element)
        }
    }

    fun dpll(cnf: CNF): Boolean {
        if (cnf.size == 0) return true
        return false
    }
}