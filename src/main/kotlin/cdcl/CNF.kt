package cdcl

import kotlin.collections.ArrayDeque
import kotlin.math.abs
import kotlin.system.exitProcess

class CNF(private val size: Int, private val clauses: MutableSet<Clause>, val literal: MutableList<Literal>) {
    private var choose = mutableListOf<Int>()

    fun copy(
        size: Int = this.size,
        clauses: MutableSet<Clause> = this.clauses.toMutableSet(),
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
                choose = o.element.run {
                    val l = maxBy { literal[abs(it) - 1].level ?: -1 }
                    filter { literal[abs(it) - 1].level == literal[abs(l!!) - 1].level }.map { abs(it) }
                }.toMutableList()
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
            println("${it.element}:${it.now}")
        }
        println("---------")
    }

    private fun getLevel(): Int {
        return literal.count { it.factor.contains(listOf(0)) }
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
                    literal[abs(i) - 1].factor.add(list.map { i -> abs(i) }.sorted())
                }
                if (cnf.literal[abs(i) - 1].bool == i <= 0) {
                    cnf.choose = mutableListOf(abs(i))
                    return cnf.backJump().oneLiteral()
                }

                cnf.clauses.filter { it.now.contains(i) }.forEach {
                    it.now.removeAll { true }
                }
                cnf.clauses.filter { it.now.contains(-i) }.forEach {
                    if (it.now.size != 1) it.now.remove(-i)
                }
                if (i > 0) cnf.literal[i - 1].bool = true
                if (i < 0) cnf.literal[abs(i) - 1].bool = false
                cnf.literal[abs(i) - 1].level = cnf.getLevel()
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
                cnf.literal[t.first - 1].level = cnf.getLevel()
            }
            if (this.isNotEmpty()) return cnf.pureLiteral()
        }
        l.filter { !it.second && !it.third && literal[it.first - 1].bool == null }.forEach {
            cnf.literal[it.first - 1].bool = true
            cnf.literal[it.first - 1].level = cnf.getLevel()
        }

        return cnf
    }

    fun literalTimes(): Int? {
        val map = mutableMapOf<Int, Int>()
        clauses.forEach { c ->
            c.now.forEach { i ->
                if (literal[abs(i) - 1].bool == null)
                    map[i] = map[i]?.plus(1) ?: 0
            }
        }
        return map.maxBy { it.value }?.key
    }

    fun setLiteral(x: Int): CNF {
        val cnf = this.copy()
        if (x > 0) cnf.literal[x - 1].bool = true
        else cnf.literal[abs(x) - 1].bool = false
        cnf.literal[abs(x) - 1].factor.add(listOf(0))
        cnf.literal[abs(x) - 1].level = cnf.getLevel()
        cnf.choose = mutableListOf(abs(x))

        cnf.clauses.filter { it.now.contains(x) }.forEach {
            it.now.removeAll { true }
        }
        cnf.clauses.filter { it.now.contains(-x) }.forEach {
            it.now.remove(-x)
        }

        return cnf
    }

    fun backJump(): CNF {
        printOut()
        if (choose.isEmpty()) choose = mutableListOf(literal.maxBy { it.level ?: 0 }?.number!!)
        val cnf = this.copy()
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
        if (changeLevel==null){
            println("UNSAT")
            exitProcess(0)
        }

        if (list.isEmpty()) return this
        cnf.clauses.add(Clause(list.joinToString(" ").plus(" 0")))
        literal.filter { it.level ?: 0 >= changeLevel || it.bool == null }.run {
            val sameLevel = mutableListOf<Int>()
            forEach {
                sameLevel.add(it.number)
            }
            cnf.clauses.forEach {
                it.element.forEach { i ->
                    if (sameLevel.contains(abs(i)))
                        it.now.add(i)
                }
            }
            sameLevel
        }.forEach {
            cnf.literal[it - 1].bool = null
            cnf.literal[it - 1].factor.removeAll { true }
            cnf.literal[it - 1].level = null
        }
        cnf.clauses.forEach {
            if (it.element.any { i ->
                    if (i > 0) literal[abs(i) - 1].bool ?: false
                    else literal[abs(i) - 1].bool?.not() ?: false
                }) it.now.removeAll { true }
        }

        printOut()
        return cnf
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun searchRoot(list: List<List<Int>>): Int {
        val all = mutableSetOf<Int>()
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
                literal[x - 1].factor.filter { !it.contains(0) }.forEach { queue.addAll(it) }
                all.add(x)
            }
        }
        return all.maxBy { all.count { i -> i == it } } ?: 0
    }
}