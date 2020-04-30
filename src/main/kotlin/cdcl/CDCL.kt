package cdcl

class CDCL(private val cnf: CNF) {
    fun run() {
        val cnf = cnf.oneLiteral().pureLiteral()

    }
}