package cdcl

import kotlin.math.abs

class CNF(val size: Int, val clauses: MutableList<Clause>, val literal: MutableList<Pair<Int, Boolean?>>) {
    val factor = mutableListOf<Int>()

    fun copy(
        size: Int = this.size,
        clauses: MutableList<Clause> = this.clauses,
        literal: MutableList<Pair<Int, Boolean?>> = this.literal
    ) = CNF(size, clauses, literal)

    fun check(): Boolean {
        clauses.forEach { o ->
            val v = mutableListOf<Boolean?>()
            o.element.forEach { i ->
                v.add(
                    if (i > 0) literal[i - 1].second
                    else literal[abs(i) - 1].second?.not()
                )
            }
            if (!v.contains(true)) return false
        }
        return true
    }

    fun printOut() {
        literal.sortedBy { it.first }.forEach {
            println("${it.first}:${it.second}")
        }
        println("---------")
    }

    fun oneLiteral(): CNF {
        val cnf = this.copy()
        val c = cnf.clauses.find { it.now.size == 1 }?.now?.first()
        if (c != null) {
            cnf.clauses.removeAll { it.now.contains(c) }
            cnf.clauses.filter { it.now.contains(-c) }.forEach {
                it.now.remove(-c)
            }
            if (c > 0) cnf.literal[c - 1] = Pair(c, true)
            if (c < 0) cnf.literal[abs(c) - 1] = Pair(-c, false)
            return cnf.oneLiteral()
        }

        return cnf
    }

    fun pureLiteral(): CNF {
        val cnf = this.copy()
        val l = mutableListOf<Triple<Int, Boolean, Boolean>>()
        repeat(cnf.size) {
            l.add(Triple(it + 1, second = false, third = false))
        }
        cnf.clauses.forEach { c ->
            c.now.forEach { e ->
                if (e > 0) l[e - 1] = Triple(l[e - 1].first, true, l[e - 1].third)
                else l[abs(e) - 1] = Triple(l[abs(e) - 1].first, l[abs(e) - 1].second, true)
            }
        }
        l.filter { it.second xor it.third }.apply {
            forEach { t ->
                cnf.clauses.removeAll { it.now.contains(t.first) || it.now.contains(-t.first) }
                if (t.second) cnf.literal[t.first - 1] = Pair(t.first, true)
                else cnf.literal[t.first - 1] = Pair(t.first, false)
            }
            if (this.isNotEmpty()) return cnf.pureLiteral()
        }

        return cnf
    }
}