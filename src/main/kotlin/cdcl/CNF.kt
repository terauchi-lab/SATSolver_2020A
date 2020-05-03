package cdcl

import kotlin.collections.ArrayDeque
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
            println("${it.element} : ${it.now}")
        }
        println("---------")
    }

    fun oneLiteral(): CNF {
        val cnf = this.copy()
        val c = cnf.clauses.filter { it.now.size == 1 }
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
                    val list = it.element.toMutableList()
                    list.remove(i)
                    literal[abs(i) - 1].factor.addAll(list)
                }
                if (cnf.literal[abs(i) - 1].bool != null) {
                    cnf.choose = abs(i)
                    return cnf.backJump().first
                }

                cnf.clauses.filter { it.now.contains(i) }.forEach {
                    it.now.removeAll { true }
                }
                cnf.clauses.filter { it.now.contains(-i) }.forEach {
                    it.now.remove(-i)
                }
                if (i > 0) cnf.literal[i - 1].bool = true
                if (i < 0) cnf.literal[abs(i) - 1].bool = false
                cnf.literal[abs(i) - 1].level = level
            }
            return cnf.oneLiteral()
        }

        return this
    }

    fun pureLiteral(): CNF {
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
        l.filter { it.second xor it.third }.apply {
            forEach { t ->
                cnf.clauses.filter { it.now.contains(t.first) || it.now.contains(-t.first) }.forEach {
                    it.now.removeAll { true }
                }
                cnf.literal[t.first - 1].bool = t.second
            }
            if (this.isNotEmpty()) return cnf.pureLiteral()
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

    fun backJump(): Pair<CNF, Int> {
        if (choose == null) return Pair(this, -1)
        val cnf = this.copy()
        val changeLevel = literal[choose!! - 1].level
        val fact = literal[choose!! - 1].factor.toMutableList()
        val root = searchRoot(fact)
        val list = mutableListOf<Int>()
        val set = mutableSetOf<Int>()
        literal.filter { it.factor.contains(root) }.forEach {
            set.addAll(it.factor)
        }
        set.filter { it != 0 }.forEach {
            list.add(
                if (literal[it - 1].bool!!) -it
                else it
            )
        }

        if (list.isEmpty()) return Pair(this, -2)
        cnf.clauses.add(0, Clause(list.joinToString(" ").plus(" 0")))
        literal.filter { it.level == changeLevel }.run {
            val sameLevel = mutableListOf<Int>()
            forEach {
                sameLevel.add(it.number)
            }
            sameLevel
        }.forEach {
            cnf.literal[it - 1].bool = null
            cnf.literal[it - 1].factor.removeAll { true }
            cnf.literal[it - 1].level = null
        }
        return Pair(cnf, --level)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun searchRoot(list: List<Int>): Int {
        val all = list.toMutableList()
        val queue = ArrayDeque<Int>()
        queue.addAll(list)
        while (queue.isNotEmpty()) {
            val x = queue.removeFirst()
            if (all.contains(x)) return x
            all.add(x)
            queue.addAll(literal[x - 1].factor)
        }
        return 0
    }
}