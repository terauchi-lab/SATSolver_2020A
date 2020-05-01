package cdcl

import kotlin.math.abs

class CNF(val size: Int, val clauses: MutableList<Clause>, val literal: MutableList<Literal>) {
    var choose = null

    fun copy(
        size: Int = this.size,
        clauses: MutableList<Clause> = this.clauses.toMutableList(),
        literal: MutableList<Literal> = this.literal.toMutableList()
    ) = CNF(size, clauses, literal)

    fun check(): Boolean {
        clauses.forEach { o ->
            val v = mutableListOf<Boolean?>()
            o.element.forEach { i ->
                v.add(
                    if (i > 0) literal[i - 1].bool
                    else literal[abs(i) - 1].bool?.not()
                )
            }
            if (v.contains(null) || !v.contains(true)) return false
        }
        return true
    }

    fun printOut() {
        literal.sortedBy { it.number }.forEach {
            println("${it.number}:${it.bool}")
        }
        println("---------")
    }

    fun oneLiteral(): CNF {
        val cnf = this.copy()
        val c = cnf.clauses.find { it.now.size == 1 }
        val v = c?.now?.first()
        if (v != null) {
            val f = c.element.toMutableList()
            f.removeAll { c.now.contains(it) }
            cnf.clauses.filter { it.now.contains(v) }.forEach {
                it.now.removeAll { true }
            }
            cnf.clauses.filter { it.now.contains(-v) }.forEach {
                it.now.remove(-v)
            }
            if (v > 0) cnf.literal[v - 1].bool = true
            if (v < 0) cnf.literal[abs(v) - 1].bool = false
            cnf.literal[abs(v) - 1].factor.addAll(f)
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
                cnf.literal[t.first - 1].bool = t.second
            }
            if (this.isNotEmpty()) return cnf.pureLiteral()
        }

        return cnf
    }

    fun literalTimes(): List<Int> {
        val list = mutableListOf<Int>()
        val map = mutableMapOf<Int, Int>()
        clauses.forEach { c ->
            c.now.forEach { i ->
                map[i] = map[i]?.plus(1) ?: 0
            }
        }
        map.toList().sortedByDescending { it.second }.forEach {
            list.add(it.first)
        }
        return list
    }
}