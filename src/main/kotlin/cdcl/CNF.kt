package cdcl

import kotlin.math.abs

class CNF(private val size: Int, private val clauses: MutableSet<Clause>, val literal: MutableList<Literal>) {
    private var choose = mutableListOf<Int>()
    private var conflict = mutableSetOf<Int>()

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
                choose = o.element.run {
                    val l = maxBy { literal[abs(it) - 1].level ?: -1 }
                    filter { literal[abs(it) - 1].level == literal[abs(l!!) - 1].level }.map { abs(it) }
                }.toMutableList()
                conflict = o.element.toMutableSet()
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
    }

    private fun getLevel(): Int {
        return literal.count { it.factor.contains(listOf(0)) }
    }

    fun oneLiteral(): CNF {
        if (finish) return this
        val c = clauses.filter { it.now.size == 1 }
        val v = c.run {
            val set = mutableSetOf<Int>()
            forEach {
                set.add(it.now.first())
            }
            set
        }
        if (v.isNotEmpty()) {
            v.forEach { i ->
                c.filter { it.now.contains(i) }.forEach {
                    val list = it.element.map { e -> abs(e) }.sorted().toMutableList()
                    list.remove(abs(i))
                    literal[abs(i) - 1].factor.add(list)
                    list.forEach { l ->
                        literal[l - 1].edge.add(Pair(abs(i), it.element))
                    }
                }
                if (literal[abs(i) - 1].bool == i <= 0) {
                    choose = mutableListOf(abs(i))
                    conflict = c.filter { it.now.contains(i) }.run {
                        val list = mutableSetOf<Int>()
                        forEach { e ->
                            list.addAll(e.element)
                        }
                        list
                    }
                    return backJump().oneLiteral()
                }

                clauses.filter { it.now.contains(i) }.forEach {
                    it.now.removeAll { true }
                }
                clauses.filter { it.now.contains(-i) }.forEach {
                    if (it.now.size != 1) it.now.remove(-i)
                }
                if (i > 0) literal[i - 1].bool = true
                if (i < 0) literal[abs(i) - 1].bool = false
                literal[abs(i) - 1].level = getLevel()
            }
            return oneLiteral()
        }
        return this
    }

    fun pureLiteral(): CNF {
        val l = mutableListOf<Triple<Int, Boolean, Boolean>>()
        (1..size).forEach {
            l.add(Triple(it, second = false, third = false))
        }
        clauses.forEach { o ->
            o.now.forEach { i ->
                if (i > 0) l[i - 1] = Triple(l[i - 1].first, true, l[i - 1].third)
                else l[abs(i) - 1] = Triple(l[abs(i) - 1].first, l[abs(i) - 1].second, true)
            }
        }
        l.filter { it.second xor it.third }.apply {
            forEach { t ->
                clauses.filter { it.now.contains(t.first) || it.now.contains(-t.first) }.forEach {
                    it.now.removeAll { true }
                }
                literal[t.first - 1].bool = t.second
                literal[t.first - 1].level = getLevel()
            }
            if (this.isNotEmpty()) return pureLiteral()
        }
        l.filter { !it.second && !it.third && literal[it.first - 1].bool == null }.forEach {
            literal[it.first - 1].bool = true
            literal[it.first - 1].level = getLevel()
        }

        return this
    }

    fun literalTimes(): Int? {
        val map = mutableMapOf<Int, Int>()
        clauses.forEach { c ->
            c.now.forEach { i ->
                if (literal[abs(i) - 1].bool == null)
                    map[i] = map[i]?.plus(1) ?: 0
            }
        }
        return map.maxBy { it.value }?.key?.let {
            clauses.filter { c -> c.now.size == 2 && c.now.contains(it) }.run {
                if (isEmpty()) return it
                val list = first().now.toMutableList()
                list.remove(it)
                list.first()
            }
        }
    }

    fun setLiteral(x: Int): CNF {
        if (x > 0) literal[x - 1].bool = true
        else literal[abs(x) - 1].bool = false
        literal[abs(x) - 1].factor.add(listOf(0))
        literal[abs(x) - 1].level = getLevel()
        choose = mutableListOf(abs(x))

        clauses.filter { it.now.contains(x) }.forEach {
            it.now.removeAll { true }
        }
        clauses.filter { it.now.contains(-x) }.forEach {
            it.now.remove(-x)
        }

        return this
    }

    fun backJump(): CNF {
        if (choose.isEmpty()) choose = mutableListOf(literal.maxBy { it.level ?: 0 }?.number!!)
        findUIP()
        val list = conflict.toMutableList()
        val changeLevel = list.run {
            val levels = mutableListOf<Int>()
            forEach { levels.add(literal[abs(it) - 1].level!!) }
            levels.min()
        }
        if (changeLevel == null) {
            println("UNSAT")
            finish = true
            return this
        }

        if (list.isEmpty()) return this
        clauses.add(Clause(list.joinToString(" ").plus(" 0")))
        literal.filter { it.level ?: 0 >= changeLevel || it.bool == null }.run {
            val sameLevel = mutableListOf<Int>()
            forEach {
                sameLevel.add(it.number)
            }
            clauses.forEach {
                it.element.forEach { i ->
                    if (sameLevel.contains(abs(i)))
                        it.now.add(i)
                }
            }
            sameLevel
        }.forEach {
            literal[it - 1].bool = null
            literal[it - 1].factor.removeAll { true }
            literal[it - 1].level = null
            literal[it - 1].edge.removeAll { true }
        }
        clauses.forEach {
            if (it.element.any { i ->
                    if (i > 0) literal[abs(i) - 1].bool ?: false
                    else literal[abs(i) - 1].bool?.not() ?: false
                }) it.now.removeAll { true }
        }

        return this
    }

    private fun findUIP() {
        val level = getLevel()
        val decision = literal.find { it.level == level && it.factor.contains(listOf(0)) }!!.number
        val visit = conflict.map { abs(it) }.toMutableSet()
        while (true) {
            val set = literal.filter { conflict.map { i -> abs(i) }.contains(it.number) }
            val next =
                literal.find {
                    set.any { l -> l.factor.any { e -> e.contains(it.number) } }
                            && it.level == level && !visit.contains(it.number)
                } ?: break
            next.edge.find { conflict.map { i -> abs(i) }.contains(it.first) }?.second?.forEach {
                if (conflict.contains(-it)) conflict.remove(-it)
                else conflict.add(it)
            }
            if (conflict.count { literal[abs(it) - 1].level == level } == 1) return
            visit.add(next.number)
            if (next.number == decision) return
        }
    }
}