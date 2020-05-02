package cdcl

class CDCL(private val cnf: CNF) {
    fun run() {
        val cnf = cnf.oneLiteral().pureLiteral(true)

        cdcl(cnf)
    }

    private fun cdcl(cnf: CNF) {
        val lite = chooseLiteral(cnf)
        val c = if (lite != null) cnf.setLiteral(lite).pureLiteral() else cnf
        //else cnf.literal.filter { it.bool == null }.forEach { it.bool = true }
        if (c.check()) {
            c.printOut()
            return
        } else if (c.literal.any { it.bool == null }) {
            cdcl(c.copy())
        } else {
            cdcl(c.backJump())
        }
        println("no")
    }

    private fun chooseLiteral(cnf: CNF): Int? {
        val last = cnf.lastOne()
        if (last != null) return last
        val list = cnf.literalTimes()
        return if (list.isNotEmpty()) list.first() else null
    }
}