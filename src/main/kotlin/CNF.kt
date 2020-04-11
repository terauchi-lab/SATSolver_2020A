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
        val c = clauses.find { it.element.size == 1 }?.element?.first()
        if (c != null) {
            clauses.removeAll { it.element.contains(c) }
            clauses.filter { it.element.contains(-c) }.forEach {
                it.element.remove(-c)
            }
            if (c > 0) literal[c - 1] = Triple(c, second = true, third = true)
            if (c < 0) literal[abs(c) - 1] = Triple(-c, second = false, third = true)
            return this.oneLiteral()
        }

        return this
    }
}