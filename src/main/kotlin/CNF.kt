import kotlin.math.abs

class CNF(val size: Int, val clauses: List<Clause>, private val literal: List<Pair<Int, Boolean>>) {
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
}