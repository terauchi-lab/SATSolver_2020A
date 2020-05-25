package cdcl

@JvmField
var finish = false

class CDCL(private val cnf: CNF) {
    fun run() {
        val cnf = cnf.oneLiteral().pureLiteral()

        cdcl(cnf)
    }

    private fun cdcl(c: CNF) {
        var cnf = c
        while (true) {
            if (finish) return
            val lite = chooseLiteral(cnf)
            if (lite != null) cnf.setLiteral(lite).oneLiteral().pureLiteral()
            if (finish) return
            when {
                cnf.check() -> {
                    c.printOut()
                    println("SAT")
                    return
                }
                cnf.literal.any { it.bool == null } -> {
                }
                else -> {
                    cnf = c.backJump().oneLiteral().pureLiteral()
                }
            }
        }
    }

    private fun chooseLiteral(cnf: CNF): Int? {
        return cnf.literalTimes()
    }
}