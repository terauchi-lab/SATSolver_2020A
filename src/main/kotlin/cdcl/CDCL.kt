package cdcl

class CDCL(private val cnf: CNF) {
    companion object {
        var finish = false
        var cnt = 0
    }

    fun run() {
        val cnf = cnf.oneLiteral().pureLiteral()
        cnf.initVSIDS()

        cdcl(cnf)
        println("conflicts: $cnt")
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
                cnf.literal.any { it.bool == null } -> Unit
                else -> {
                    cnf = c.dummyJump().oneLiteral().pureLiteral()
                }
            }
        }
    }

    private fun chooseLiteral(cnf: CNF): Int? {
        return cnf.getVSIDS()
    }
}