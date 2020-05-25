package cdcl

@JvmField
var finish = false

class CDCL(private val cnf: CNF) {
    fun run() {
        val cnf = cnf.oneLiteral().pureLiteral()

        cdcl(cnf)
    }

    private fun cdcl(cnf: CNF) {
        if (finish) return
        val lite = chooseLiteral(cnf)
        val c = if (lite != null) cnf.setLiteral(lite).oneLiteral().pureLiteral() else cnf
        if (finish) return
        when {
            c.check() -> {
                c.printOut()
                println("SAT")
                return
            }
            c.literal.any { it.bool == null } -> {
                cdcl(c)
            }
            else -> {
                cdcl(c.backJump().oneLiteral().pureLiteral())
            }
        }
    }

    private fun chooseLiteral(cnf: CNF): Int? {
        return cnf.literalTimes()
    }
}