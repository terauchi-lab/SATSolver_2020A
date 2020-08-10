package dpll

import java.io.File
import kotlin.system.measureNanoTime

class Solve(private val file: File) {
    companion object {
        var cnt = 0
    }

    fun run() {
        println("Solving by DPLL")
        val texts = file.readLines()

        val size = texts[0].split(' ')[2].toInt()
        val clause = mutableListOf<Clause>()
        texts.drop(1).forEach {
            clause.add(Clause(it))
        }
        val literal = mutableListOf<Triple<Int, Boolean, Boolean>>()
        repeat(size) {
            literal.add(Triple(it + 1, second = true, third = false))
        }

        val cnf = CNF(size, clause, literal)

        println(
            measureNanoTime {
                dpll(cnf).run {
                    println("conflicts: $cnt")
                    println(if (this) "SAT" else "UNSAT")
                }
            } / 1e9
        )
    }

    private fun dpll(c: CNF): Boolean {
        val cnf = c.oneLiteral().pureLiteral()

        if (cnf.size == 0 || cnf.clauses.size == 0) {
            cnf.printOut()
            return true
        }
        if (cnf.check()) {
            cnf.printOut()
            return true
        }
        val v = cnf.literal.find { !it.third }?.first
        if (v != null) {
            val onTrue = cnf.literal.toMutableList()
            val onFalse = cnf.literal.toMutableList()
            onTrue[v - 1] = Triple(v, second = true, third = true)
            onFalse[v - 1] = Triple(v, second = false, third = true)
            if (dpll(CNF(cnf.size, cnf.clauses, onTrue))) return true
            if (dpll(CNF(cnf.size, cnf.clauses, onFalse))) return true
        }
        cnt++
        return false
    }
}