package cdcl

class CDCL(private val cnf: CNF) {
    fun run() {
        val cnf = cnf.oneLiteral().pureLiteral()

        cdcl(cnf)
    }

    fun cdcl(cnf: CNF) {
        val list = cnf.literalTimes()
        cnf.setLiteral(list.first())
        val c = cnf.pureLiteral()
        if (c.check()) {
            c.printOut()
            return
        } else if (c.literal.any { it.bool == null }) {
            cdcl(c)
        }
        println("no")
    }
}