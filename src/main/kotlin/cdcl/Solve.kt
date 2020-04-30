package cdcl

import java.io.File
import kotlin.system.measureNanoTime

class Solve(private val file: File) {
    fun run() {
        val texts = file.readLines()

        val size = texts[0].split(' ')[2].toInt()
        val clause = mutableListOf<Clause>()
        texts.drop(1).forEach {
            clause.add(Clause(it))
        }
        val literal = mutableListOf<Pair<Int, Boolean?>>()
        (1..size).forEach {
            literal.add(Pair(it + 1, null))
        }

        val cnf = CNF(size, clause, literal)

        println(
            measureNanoTime {
                println(cdcl(cnf))
            }
        )
    }

    fun cdcl(c: CNF): Boolean {
        val cnf = c.oneLiteral().pureLiteral()

        return false
    }
}