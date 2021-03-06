package cdcl

import java.io.File
import kotlin.system.measureNanoTime

class Solve(private val file: File) {
    fun run() {
        println("Solving by CDCL")
        val texts = file.readLines().dropWhile { it.first() == 'c' }

        val size = texts[0].split(' ')[2].toInt()
        val clause = mutableSetOf<Clause>()
        texts.drop(1).forEach {
            clause.add(Clause(it))
        }
        val literal = mutableListOf<Literal>()
        (1..size).forEach {
            literal.add(Literal(it, null, null, mutableListOf(), mutableListOf(), null))
        }

        val cnf = CNF(size, clause, literal)

        println(
            "run: ${
            measureNanoTime {
                CDCL(cnf).run()
            } / 1e9
            }"
        )
    }
}