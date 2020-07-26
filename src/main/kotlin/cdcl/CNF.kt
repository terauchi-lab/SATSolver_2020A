package cdcl

import kotlin.math.abs

class CNF(private val size: Int, private val clauses: MutableSet<Clause>, val literal: MutableList<Literal>) {
    private var choose = mutableListOf<Int>()
    private var conflict = mutableSetOf<Int>()
    private val count = mutableListOf<Triple<Int, Int, Int>>()

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

    private fun onFailed() {
        println("UNSAT")
        CDCL.finish = true
    }

    private fun getLevel(): Int {
        return literal.count { it.factor.contains(listOf(0)) }
    }

    fun oneLiteral(): CNF {
        if (CDCL.finish) return this
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
                    literal[abs(i) - 1].apply {
                        factor.add(list)
                        deep = factor.let { f ->
                            val l = mutableListOf<Int>()
                            f.forEach { ff ->
                                ff.forEach { e ->
                                    l.add(literal[e - 1].deep ?: 0)
                                }
                            }
                            l.max()
                        }?.plus(1)
                    }
                    list.forEach { l ->
                        literal[abs(i) - 1].edge.add(Pair(abs(l), it.element))
                    }
                }
                if (literal[abs(i) - 1].bool == i <= 0) {
                    choose = mutableListOf(abs(i))
                    conflict = literal[abs(i) - 1].factor.run {
                        val list = mutableSetOf<Int>()
                        forEach { e ->
                            list.addAll(e)
                        }
                        list.remove(i)
                        list.remove(-i)
                        list
                    }
                    return dummyJump().oneLiteral()
                }

                clauses.filter { it.now.contains(i) }.forEach {
                    it.now.clear()
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
        return map.maxBy { it.value }?.key
    }

    fun initVSIDS() {
        repeat(size) { count.add(Triple(0, 0, it)) }
        clauses.forEach { o ->
            o.element.forEach {
                if (it < 0) count[abs(it) - 1] = count[abs(it) - 1].run { Triple(first, second + 1, third) }
                else count[it - 1] = count[it - 1].run { Triple(first + 1, second, third) }
            }
        }
    }

    private fun vsids(list: List<Int>) {
        for (i in count.indices) {
            count[i] = count[i].run { Triple(first / 2, second / 2, third) }
        }
        list.forEach {
            if (it < 0) count[abs(it) - 1] = count[abs(it) - 1].run { Triple(first, second + 4, third) }
            else count[it - 1] = count[it - 1].run { Triple(first + 4, second, third) }
        }
    }

    fun getVSIDS(): Int? =
        count.filter { literal[it.third].bool == null }.maxBy { maxOf(it.first, it.second) }?.run {
            val id = third + 1
            if (first > second) id
            else -id
        }

    fun setLiteral(x: Int): CNF {
        if (x > 0) literal[x - 1].bool = true
        else literal[abs(x) - 1].bool = false
        literal[abs(x) - 1].apply {
            factor.add(listOf(0))
            level = getLevel()
            deep = 0
        }
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
            onFailed()
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
            literal[it - 1].run {
                bool = null
                factor.removeAll { true }
                level = null
                edge.removeAll { true }
            }
        }
        clauses.forEach {
            if (it.element.any { i ->
                    if (i > 0) literal[abs(i) - 1].bool ?: false
                    else literal[abs(i) - 1].bool?.not() ?: false
                }) it.now.removeAll { true }
        }
        CDCL.cnt++
        vsids(list)
        return this
    }

    private fun findUIP() {
        val level = getLevel()
        val decision = literal.find { it.level == level && it.factor.contains(listOf(0)) }?.number
        if (decision == null) onFailed()
        val set = literal.filter { conflict.map { i -> abs(i) }.contains(it.number) }.toMutableSet()
        val use = mutableSetOf<Int>()
        while (true) {
            if (conflict.count { literal[abs(it) - 1].level == level } == 1) {
                if (conflict.find { literal[abs(it) - 1].level == level } ?: 0 == choose.first())
                    onFailed()
                return
            }
            val edges =
                set.filter {
                    use.contains(it.number).not() &&
                            it.factor.any { e ->
                                e.any { l ->
                                    (if (l != 0) literal[abs(l) - 1].level == level else false)
                                }
                            }
                }.maxBy { it.deep ?: 0 }?.run {
                    use.add(number)
                    edge
                } ?: break
            val next =
                edges.find { literal[it.first - 1].level == level} ?: break
            next.second.forEach {
                if (conflict.contains(-it)) conflict.remove(-it)
                else conflict.add(it)
            }
            set.addAll(literal.filter { conflict.map { i -> abs(i) }.contains(it.number) })
            if (next.first == decision) return
        }
    }

    fun dummyJump(): CNF {
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
            val l = mutableSetOf<Int>()
            forEach {
                l.add(literal[it - 1].level!!)
            }
            l.remove(0)
            l.min()
        }
        if (changeLevel == null) {
            onFailed()
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
            literal[it - 1].apply {
                bool = null
                factor.clear()
                level = null
                edge.clear()
            }
        }
        clauses.forEach {
            if (it.element.any { i ->
                    if (i > 0) literal[abs(i) - 1].bool ?: false
                    else literal[abs(i) - 1].bool?.not() ?: false
                }) it.now.removeAll { true }
        }
        vsids(list)
        CDCL.cnt++
        return this
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
                return x
            } else {
                literal[x - 1].factor.filter { !it.contains(0) }.forEach {
                    queue.addAll(it)
                }
                all.add(x)
            }
        }
        return all.maxBy { all.count { i -> i == it } } ?: 0
    }
}