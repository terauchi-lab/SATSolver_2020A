package cdcl

@JvmField
var level = 0

class CDCL(private val cnf: CNF) {
    fun run() {
        val cnf = cnf.oneLiteral().pureLiteral()

        cdcl(cnf)
    }

    private fun cdcl(cnf: CNF) {
        val lite = chooseLiteral(cnf)
        val c = if (lite != null) cnf.setLiteral(lite).oneLiteral().pureLiteral() else cnf
        when {
            c.check() -> {
                c.printOut()
                println("SAT")
                return
            }
            c.literal.any { it.bool == null } -> {
                cdcl(c.copy())
            }
            else -> {
                cdcl(c.backJump().oneLiteral().pureLiteral())
            }
        }
    }

    private fun chooseLiteral(cnf: CNF): Int? {
        level++
        return cnf.literalTimes()
    }
}