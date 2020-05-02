package cdcl

import kotlin.math.abs

class CNF(val size: Int, val clauses: MutableList<Clause>, val literal: MutableList<Literal>) {
    var choose: Int? = null

    fun copy(
        size: Int = this.size,
        clauses: MutableList<Clause> = this.clauses.toMutableList(),
        literal: MutableList<Literal> = this.literal.toMutableList()
    ) = CNF(size, clauses, literal).also {
        it.choose = this.choose
    }

    fun check(): Boolean {
        clauses.forEach { o ->
            val v = mutableListOf<Boolean?>()
            o.element.forEach { i ->
                v.add(
                    if (i > 0) literal[i - 1].bool
                    else literal[abs(i) - 1].bool?.not()
                )
            }
            if (v.contains(null)) return false
            if (!v.contains(true)) {
                //choose = abs(o.element.maxBy { literal[abs(it) - 1].factor.size }!!)
                return false
            }
        }
        return true
    }

    fun printOut() {
        literal.sortedBy { it.number }.forEach {
            println("${it.number}:${it.bool}")
        }
        println("---------")
        clauses.forEach {
            println(it.now)
        }
        println("---------")
    }

    fun oneLiteral(level: Int): CNF {
        val cnf = this.copy()
        val v = cnf.clauses.find { it.now.size == 1 }?.now?.first()
        if (v != null) {
            cnf.clauses.filter { it.now.contains(v) }.forEach {
                it.now.removeAll { true }
            }
            cnf.clauses.filter { it.now.contains(-v) }.forEach {
                it.now.remove(-v)
            }
            if (v > 0) cnf.literal[v - 1].bool = true
            if (v < 0) cnf.literal[abs(v) - 1].bool = false
            cnf.literal[abs(v) - 1].level = level
            return cnf.oneLiteral(level)
        }

        return this
    }

    fun pureLiteral(isFirst: Boolean = false): CNF {
        val cnf = this.copy()
        val l = mutableListOf<Triple<Int, Boolean, Boolean>>()
        (1..cnf.size).forEach {
            l.add(Triple(it, second = false, third = false))
        }
        cnf.clauses.forEach { o ->
            o.now.forEach { i ->
                if (i > 0) l[i - 1] = Triple(l[i - 1].first, true, l[i - 1].third)
                else l[abs(i) - 1] = Triple(l[abs(i) - 1].first, l[abs(i) - 1].second, true)
            }
        }
        if (isFirst) {
            l.filter { it.second xor it.third }.apply {
                forEach { t ->
                    cnf.clauses.removeAll { it.now.contains(t.first) || it.now.contains(-t.first) }
                    cnf.literal[t.first - 1].bool = t.second
                }
                if (this.isNotEmpty()) return cnf.pureLiteral()
            }
        }
        l.filter { !it.second && !it.third && literal[it.first - 1].bool == null }.forEach {
            cnf.literal[it.first - 1].bool = true
        }

        return cnf
    }

    fun literalTimes(): MutableList<Int> {
        val list = mutableListOf<Int>()
        val map = mutableMapOf<Int, Int>()
        clauses.forEach { c ->
            c.now.forEach { i ->
                if (literal[abs(i) - 1].bool == null)
                    map[i] = map[i]?.plus(1) ?: 0
            }
        }
        map.toList().sortedByDescending { it.second }.forEach {
            list.add(it.first)
        }
        return list
    }

    fun lastOne(): Int? {
        clauses.forEach { c ->
            val list = c.now.map { abs(it) }
            if (list.count { literal[it - 1].bool == null } == 1) {
                val e = c.now.find { literal[abs(it) - 1].bool == null }!!
                val fact = c.now.toMutableList()
                fact.remove(e)
                literal[abs(e) - 1].factor.addAll(fact.map { abs(it) })
                return e
            }
        }
        return null
    }

    fun setLiteral(x: Int, level: Int): CNF {
        val cnf = this.copy()
        if (x > 0) cnf.literal[x - 1].bool = true
        else cnf.literal[abs(x) - 1].bool = false
        cnf.literal[abs(x) - 1].factor.add(0)
        cnf.literal[abs(x) - 1].level = level
        cnf.choose = abs(x)

        cnf.clauses.filter { it.now.contains(x) }.forEach {
            it.now.removeAll { true }
        }
        cnf.clauses.filter { it.now.contains(-x) }.forEach {
            it.now.remove(-x)
        }

        cnf.printOut()
        return cnf
    }

    fun backJump(): CNF {
        if (choose == null) return this
        val cnf = this.copy()
        val fact = literal[choose!! - 1].factor.toMutableList()
        val list = mutableListOf<Int>()
        val set = mutableSetOf<Int>()
        fact.filter { it != 0 }.forEach {
            if (literal[it - 1].factor.isNotEmpty()) {
                set.add(it)
                set.addAll(literal[it - 1].factor.filter { it != 0 })
            }
        }
        set.forEach {
            list.add(
                if (literal[it - 1].bool!!) -it
                else it
            )
        }

        if (list.isEmpty()) return this
        cnf.clauses.add(0, Clause(list.joinToString(" ").plus(" 0")))
        set.forEach {
            cnf.literal[it - 1].bool = null
            cnf.literal[it - 1].factor.removeAll { true }
        }
        return cnf
    }
}