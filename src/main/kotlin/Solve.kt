import java.io.File

class Solve(private val file: File) {
    fun run() {
        CNF(file.readLines().drop(1)).clauses.forEach {
            println(it.literals)
        }
    }
}