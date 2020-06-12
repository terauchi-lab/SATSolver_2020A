package cdcl

import kotlin.collections.ArrayDeque
import kotlin.math.abs

class CNF(private val size: Int, private val clauses: MutableSet<Clause>, val literal: MutableList<Literal>) {
    private var choose = mutableListOf<Int>()
    private var conflict = mutableListOf<Int>()

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
                conflict = o.element.toMutableList()
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
                        list.toMutableList()
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
        val fact = choose.run {
            val l = mutableListOf<List<Int>>()
            forEach {
                l.addAll(literal[it - 1].factor.filter { i -> !i.contains(0) })
            }
            l
        }
        val root = searchRoot(fact)
        val list = mutableListOf<Int>()
        val set = mutableSetOf<Int>()
        if (fact.size != fact.toSet().size) {
            fact.forEach {
                if (fact.count { i -> i == it } > 1) set.addAll(it)
            }
        } else {
            literal.filter { it.factor.any { e -> e.contains(root) } }.forEach {
                it.factor.filter { l -> l.contains(root) }.forEach { l ->
                    set.addAll(l)
                }
            }
        }
        set.filter { it != 0 }.forEach {
            list.add(
                if (literal[it - 1].bool!!) -it
                else it
            )
        }
        val changeLevel = set.run {
            val l = mutableListOf<Int>()
            forEach {
                l.add(literal[it - 1].level!!)
            }
            l.min()
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

    fun findUIP(): List<Int> {
        val level = getLevel()
        val decision = literal.find { it.level == level && it.factor.contains(listOf(0)) }!!.number

        return listOf(0)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun searchRoot(list: List<List<Int>>): Int {
        val all = mutableListOf<Int>()
        val queue = ArrayDeque<Int>()
        list.forEach {
            queue.addAll(it)
        }
        while (queue.isNotEmpty()) {
            val x = queue.removeFirst()
            if (all.contains(x)) {
                if (literal.filter { it.factor.any { e -> e.contains(x) } }.run {
                        val set = mutableSetOf<Int>()
                        forEach {
                            it.factor.filter { l -> l.contains(x) }.forEach { l ->
                                set.addAll(l)
                            }
                        }
                        set.any { literal[it - 1].factor.contains(listOf(0)) }
                    })
                    return x
            } else {
                literal[x - 1].factor.filter { !it.contains(0) }.forEach {
                    queue.addAll(it)
                    all.addAll(it)
                }
                all.add(x)
            }
        }
        return all.maxBy { all.count { i -> i == it } } ?: 0
    }
}