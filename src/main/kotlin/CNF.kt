import kotlin.math.abs

class CNF(val size: Int, val clauses: MutableList<Clause>, val literal: MutableList<Triple<Int, Boolean, Boolean>>) {
    fun check(): Boolean {
        val c = mutableListOf<Boolean>()
        clauses.forEach { o ->
            val v = mutableListOf<Boolean>()
            o.element.forEach { i ->
                v.add(
                    if (i > 0) literal[i - 1].second
                    else !literal[abs(i) - 1].second
                )
            }
            c.add(v.contains(true))
        }
        if (c.contains(false)) return false
        return true
    }

    fun printOut() {
        literal.sortedBy { it.first }.forEach {
            println("${it.first}:${it.second}")
        }
    }

    fun oneLiteral(): CNF {
        val cnf = this
        val c = cnf.clauses.find { it.element.size == 1 }?.element?.first()
        if (c != null) {
            cnf.clauses.removeAll { it.element.contains(c) }
            cnf.clauses.filter { it.element.contains(-c) }.forEach {
                it.element.remove(-c)
            }
            if (c > 0) cnf.literal[c - 1] = Triple(c, second = true, third = true)
            if (c < 0) cnf.literal[abs(c) - 1] = Triple(-c, second = false, third = true)
            return cnf.oneLiteral()
        }

        return cnf
    }

    fun pureLiteral(): CNF {
        val cnf = this
        val l = mutableListOf<Triple<Int, Boolean, Boolean>>()
        repeat(size) {
            l.add(Triple(it + 1, second = false, third = false))
        }
        cnf.clauses.forEach { c ->
            c.element.forEach { e ->
                if (e > 0) l[e - 1] = Triple(l[e - 1].first, true, l[e - 1].third)
                else l[abs(e) - 1] = Triple(l[abs(e) - 1].first, l[abs(e) - 1].second, true)
            }
        }
        l.filter { it.second xor it.third }.forEach { t ->
            cnf.clauses.removeAll { it.element.contains(t.first) || it.element.contains(-t.first) }
            if (t.second) cnf.literal[t.first - 1] = Triple(t.first, second = true, third = true)
            else cnf.literal[t.first - 1] = Triple(t.first, second = false, third = true)
        }

        return cnf
    }
}